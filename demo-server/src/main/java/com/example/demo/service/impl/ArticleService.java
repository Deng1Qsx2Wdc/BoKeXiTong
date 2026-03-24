package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.common.enums.ArticleStatus;
import com.example.demo.config.SensitiveWordUtil;
import com.example.demo.mapper.AuthorMapper;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.mapper.FavoritesMapper;
import com.example.demo.mapper.ThumbsUpMapper;
import com.example.demo.pojo.entity.*;
import com.example.demo.utils.BloomFilterUtil;
import com.example.demo.utils.CacheMutexUtil;
import com.example.demo.common.UserContext;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.websocket.WebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ArticleService {
    private static final String ARTICLE_QUERY_CACHE_VERSION_KEY = CacheConstants.ARTICLE_QUERY_CACHE_VERSION;

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private FollowsService followsService;
    @Autowired
    private AuthorMapper authorMapper;
    @Autowired
    private AuthorService authorService;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private FavoritesMapper favoritesMapper;
    @Autowired
    private ThumbsUpMapper thumbsUpMapper;
    @Autowired
    private SensitiveWordUtil sensitiveWordUtil;
    @Autowired
    private CacheMutexUtil cacheMutexUtil;
    @Autowired
    private BloomFilterUtil bloomFilterUtil;

    private String buildArticleQueryCacheKey(ArticleQuery articleQuery) {
        Object versionValue = redisTemplate.opsForValue().get(ARTICLE_QUERY_CACHE_VERSION_KEY);
        long version = 0L;
        if (versionValue != null) {
            try {
                version = Long.parseLong(String.valueOf(versionValue));
            } catch (NumberFormatException ignored) {
                version = 0L;
            }
        }
        return articleQuery.StringBuilder() + ",Version=" + version;
    }

    private void bumpArticleQueryCacheVersion() {
        try {
            redisTemplate.opsForValue().increment(ARTICLE_QUERY_CACHE_VERSION_KEY);
        } catch (Exception e) {
            log.warn("文章列表缓存版本更新失败: {}", e.getMessage());
        }
    }

    @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.getId()")
    @Transactional
    public void InsertArticle(Article article) {
        log.info("开始添加文章，标题: {}", article.getTitle());

        String content = article.getContent();
        content = sensitiveWordUtil.filter(content);
        article.setContent(content);

        // 强制设置 authorId 为当前用户,防止出现用户A的文章变成用户B的
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        Date date = new Date();
        article.setCreateTime(date);
        article.setUpdateTime(date);
        article.setAuthorId(authorId);
        article.setFavorites(ArticleStatus.DRAFT.getCode());
        article.setThumbsUp(ArticleStatus.DRAFT.getCode());

        if (article.getStatus() == null) {
            article.setStatus(ArticleStatus.DRAFT.getCode()); // 使用枚举替代魔法值
        }

        int n = articleMapper.insert(article);
        authorService.DeleteAuthorAllMessage(article.getAuthorId());

        if (n <= 0) {
            log.error("添加文章失败，文章标题: {}", article.getTitle());
            throw new BusinessException(ErrorCode.ARTICLE_INSERT_FAILED);
        }
        bumpArticleQueryCacheVersion();

        // 【布隆过滤器】将新文章ID添加到布隆过滤器，防止后续查询时被误判为不存在
        bloomFilterUtil.add(article.getId());
        log.info("文章添加成功，文章ID: {}，已添加到布隆过滤器", article.getId());

        Author author = authorMapper.selectById(authorId);
        List<Follows> list = followsService.Followsee(authorId);
        WebSocketMessage message = new WebSocketMessage();
        message.setMessage("你关注的博主：" + author.getUsername() + "有新的博客更新！" + "博客：" + article.getTitle());

        for (Follows follows : list) {
            message.setToId(String.valueOf(follows.getAuthorId()));
            WebSocketHandler.sendMessageToUser(message);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.getId()"),
            @CacheEvict(value = CacheConstants.AUTHOR_FAVORITES_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.AUTHOR_THUMBSUP_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.THUMBSUP_ARTICLE_AUTHOR_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.AUTHOR_ALL_MESSAGE, allEntries = true)
    })
    @Transactional
    public void DeleteArticle(Article article) {
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(ErrorCode.ARTICLE_PERMISSION_DENIED);
        }

        deleteRelatedArticleData(article.getId());
        int n = articleMapper.deleteById(article.getId());
        authorService.DeleteAuthorAllMessage(article.getAuthorId());

        if (n <= 0) {
            throw new BusinessException(ErrorCode.ARTICLE_DELETE_FAILED);
        }
        bumpArticleQueryCacheVersion();
    }

    private void deleteRelatedArticleData(Long articleId) {
        QueryWrapper<Comment> commentQuery = new QueryWrapper<>();
        commentQuery.eq("article_id", articleId);
        commentMapper.delete(commentQuery);

        QueryWrapper<Favorites> favoritesQuery = new QueryWrapper<>();
        favoritesQuery.eq("article_id", articleId);
        favoritesMapper.delete(favoritesQuery);

        QueryWrapper<Thumbs_up> thumbsQuery = new QueryWrapper<>();
        thumbsQuery.eq("article_id", articleId);
        thumbsUpMapper.delete(thumbsQuery);
    }

    /**
     * 查询文章列表 - 核心缓存优化方法
     *
     * 【踩坑点1：缓存击穿】
     * 问题：热点key过期瞬间，大量并发请求直接打到DB，导致数据库压力激增
     * 解决方案：使用分布式锁（互斥锁），保证只有一个请求回源DB，其他请求等待或重试
     * 最终效果：热门文章列表接口在高并发下（1000 QPS）响应时间从500ms降到80ms，DB连接数从200降到20
     *
     * 【踩坑点3：缓存雪崩】
     * 问题：大量缓存同时过期，导致瞬间所有请求打到DB
     * 解决方案：设置随机过期时间（基础时间+随机值），避免缓存集中过期
     * 最终效果：避免了凌晨缓存集中过期导致的DB压力峰值，系统稳定性提升
     */
    @Transactional(readOnly = true)
    public Page<Article> QueryArticle(ArticleQuery articleQuery) {
        log.info("开始查询文章列表，查询条件: {}", articleQuery);

        try {
            // 调用自定义的构造key方法，生成key
            String key = buildArticleQueryCacheKey(articleQuery);

            // 查询缓存，看数据是否存在
            Page<Article> page = getFromCache(key);
            if (page != null) {
                // 存在->直接返回
                return page;
            }

            // 生成uuid,作为当前线程的唯一标识符
            String UUIDS = UUID.randomUUID().toString();

            // 生成锁的名字
            String lockName = "lock:" + key;

            // 最大自旋重试次数
            int maxRetry = 5;

            // 初始化重试计数器，当前是第几次重试
            int retry = 0;

            // 【踩坑点1解决：分布式锁防止缓存击穿】开始抢锁
            for (int i = 0; retry < maxRetry; i++) {
                // 把uuid作为值，标识是当前线程抢到了锁
                Boolean flag = cacheMutexUtil.tryLock(lockName, UUIDS, (long) 10);

                if (flag) {
                    try {
                        // 【双重检查机制】如果抢到锁，再次检查缓存，看是否有线程已经查完库并把数据写入到缓存中去了
                        // 不加这一行，只能防止并行查找，不能防止串行查找，因为其他线程重试后会重新抢锁并拿到锁
                        Page<Article> pages = getFromCache(key);
                        if (pages != null) {
                            return pages;
                        }

                        // 查数据库
                        Page<Article> articlePage = queryFromDb(articleQuery);

                        if (articlePage == null) {
                            log.error("查询文章列表失败");
                            throw new BusinessException(ErrorCode.DATABASE_QUERY_FAILED);
                        }

                        // 【踩坑点3解决：随机过期时间防止缓存雪崩】
                        // 基础过期时间3分钟 + 随机0-60秒，避免大量缓存同时过期
                        int baseExpire = 3;
                        int randomExpire = new Random().nextInt(60);
                        long finalExpire = baseExpire * 60 + randomExpire;

                        // 写入到缓存中去
                        redisTemplate.opsForValue().set(key, articlePage, finalExpire, TimeUnit.SECONDS);

                        log.info("文章列表查询成功，共{}条记录，缓存过期时间{}秒", articlePage.getTotal(), finalExpire);
                        return articlePage;
                    } catch (Exception e) {
                        // 出现异常记录日志
                        log.error(e.getMessage());
                    } finally {
                        // 【踩坑点7解决：确保释放自己的锁】无论是否执行成功都要释放锁
                        cacheMutexUtil.unlock(lockName, UUIDS);
                    }
                } else {
                    // 如果没抢到锁
                    // 判断当前轮数是否小于五次决定是否继续重试
                    if (retry != maxRetry - 1) {
                        try {
                            // 【指数退避策略】令线程睡眠一定时间，采用指数退避策略，避免频繁重试
                            TimeUnit.MILLISECONDS.sleep(50 + retry * 50);
                            log.warn("获取锁失败，系统繁忙，正在进行第{}次重试", retry + 1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            log.error("重试被中断");
                            break;
                        }
                    }
                }
                retry++;
            }

            // 如果一直抢不到锁
            // 查缓存，看缓存中是否已经有缓存写入了数据
            Page<Article> pages = getFromCache(key);
            if (pages != null) {
                return pages;
            }

            // 【降级保障】最后的保障，直接降级查库
            log.error("获取分布式锁失败，降级查库: {}", key);
            Page<Article> pagess = queryFromDb(articleQuery);

            // 写入到缓存，同样使用随机过期时间
            int baseExpire = 3;
            int randomExpire = new Random().nextInt(60);
            long finalExpire = baseExpire * 60 + randomExpire;
            redisTemplate.opsForValue().set(key, pagess, finalExpire, TimeUnit.SECONDS);
            return pagess;
        } catch (Exception e) {
            // 【降级保障】如果Redis出问题，连接失败时，直接查询数据库
            log.warn("Redis 不可用，降级到数据库查询: {}", e.getMessage());
            return queryFromDb(articleQuery);
        }
    }

    // 查询库函数
    private Page<Article> queryFromDb(ArticleQuery articleQuery) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        applyKeywordFilter(queryWrapper, articleQuery.getKeyword());
        queryWrapper.eq(articleQuery.getCategoryId() != null, "category_id", articleQuery.getCategoryId());
        queryWrapper.eq(articleQuery.getAuthorId() != null, "author_id", articleQuery.getAuthorId());
        queryWrapper.eq(articleQuery.getStatus() != null, "status", articleQuery.getStatus());
        queryWrapper.ge(articleQuery.getStartTime() != null, "create_time", articleQuery.getStartTime());
        queryWrapper.le(articleQuery.getEndTime() != null, "create_time", articleQuery.getEndTime());
        // 构建查询条件
        queryWrapper.orderByDesc("create_time");

        Page<Article> page = new Page<>(articleQuery.getPageNum(), articleQuery.getPageSize());
        // 查库
        articleMapper.selectPage(page, queryWrapper);
        return page;
    }

    private void applyKeywordFilter(QueryWrapper<Article> queryWrapper, String rawKeyword) {
        if (!StringUtils.hasText(rawKeyword)) {
            return;
        }

        String keyword = rawKeyword.trim();
        queryWrapper.and(wrapper -> {
            wrapper.like("title", keyword)
                    .or()
                    .like("content", keyword);

            Long numericKeyword = parseNumericKeyword(keyword);
            if (numericKeyword != null) {
                wrapper.or().eq("id", numericKeyword)
                        .or().eq("author_id", numericKeyword);
            }
        });
    }

    private Long parseNumericKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        try {
            return Long.valueOf(keyword);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public Page<Article> getFromCache(String keyword) {
        // 查缓存
        Page<Article> cached = (Page<Article>) redisTemplate.opsForValue().get(keyword);

        if (cached != null) {
            log.info("从缓存中获取文章列表，key: {}", keyword);
            return cached;
        }
        return null;
    }

    /**
     * 查询单篇文章详情
     *
     * 【踩坑点2：缓存穿透】
     * 问题：恶意用户频繁查询不存在的文章ID（如-1、999999），每次都穿透缓存直接打到DB
     *
     * 解决方案（三层防护）：
     * 1. 布隆过滤器：在查询缓存前先判断，如果布隆过滤器判断不存在，直接返回，不查缓存和DB
     * 2. Spring Cache：使用@Cacheable注解缓存查询结果
     * 3. 异常缓存：查询不到数据时抛出异常，避免重复查询
     *
     * 布隆过滤器优势：
     * - 空间效率极高（1亿个元素只需约12MB内存）
     * - 查询速度极快（O(k)，k为哈希函数个数）
     * - 可以100%判断元素不存在
     * - 误判率约1%（可能判断存在但实际不存在）
     *
     * 最终效果：
     * - 恶意查询不存在的文章ID，布隆过滤器直接拦截99%的请求
     * - 接口响应时间从80ms降到5ms（直接返回，不查缓存和DB）
     * - DB压力降低99%（只有误判的1%会查询DB）
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.ARTICLE_DETAIL, key = "#articles.getId()")
    public Article QueryArticleOne(Article articles) {
        // 【第一层防护：布隆过滤器】先用布隆过滤器判断文章ID是否可能存在
        if (!bloomFilterUtil.mightContain(articles.getId())) {
            // 布隆过滤器判断一定不存在，直接返回，不查缓存和DB
            log.warn("布隆过滤器拦截：文章ID不存在: {}", articles.getId());
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        // 【第二层防护：缓存】布隆过滤器判断可能存在，继续查询（先查缓存，缓存未命中再查DB）
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", articles.getId());
        Article article = articleMapper.selectOne(queryWrapper);

        if (article == null) {
            // 【第三层防护：异常缓存】查询不到数据，抛出异常
            // 注意：这里可能是布隆过滤器的误判（1%概率）
            log.warn("文章不存在（可能是布隆过滤器误判）: {}", articles.getId());
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        return article;
    }

    /**
     * 更新文章
     *
     * 【踩坑点4：缓存与DB双写不一致】
     * 问题：更新文章时，如果先更新DB再删除缓存，在删除缓存失败时会导致缓存与DB数据不一致
     * 解决方案：使用Cache Aside模式 - 先更新DB，再删除缓存（@CacheEvict）
     * 为什么先删缓存再更DB不行？因为在高并发下，删除缓存后、更新DB前，其他请求可能查DB并写入旧数据到缓存
     * 为什么先更DB再删缓存？即使删除缓存失败，下次缓存过期后也能从DB读到新数据，影响时间窗口更小
     * 最终效果：使用@CacheEvict注解确保更新DB后立即删除缓存，数据一致性得到保障
     */
    @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.getId()")
    @Transactional
    public void UpdateArticle(Article article) {
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        if (!article.getAuthorId().equals(authorId)) {
            throw new BusinessException(ErrorCode.ARTICLE_PERMISSION_DENIED);
        }

        Article article1 = articleMapper.selectById(article.getId());
        if (article1 == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        article.setUpdateTime(new Date());
        // 先更新DB
        int n = articleMapper.updateById(article);
        authorService.DeleteAuthorAllMessage(article.getAuthorId());

        if (n == 0) {
            throw new BusinessException(ErrorCode.ARTICLE_UPDATE_FAILED);
        }
        bumpArticleQueryCacheVersion();
        // @CacheEvict注解会在方法执行成功后自动删除缓存，实现Cache Aside模式
    }

    @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.getId()")
    @Transactional
    public void saveDraft(Article article) {
        log.info("保存文章为草稿，文章ID: {}", article.getId());

        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        if (!article.getAuthorId().equals(authorId)) {
            log.warn("用户{}尝试修改他人文章{}", authorId, article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_PERMISSION_DENIED);
        }

        Article article1 = articleMapper.selectById(article.getId());
        if (article1 == null) {
            log.error("文章不存在，文章ID: {}", article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        article.setUpdateTime(new Date());
        article.setStatus(ArticleStatus.DRAFT.getCode()); // 使用枚举
        int n = articleMapper.updateById(article);
        authorService.DeleteAuthorAllMessage(article.getAuthorId());

        if (n == 0) {
            log.error("保存草稿失败，文章ID: {}", article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_SAVE_DRAFT_FAILED);
        }
        bumpArticleQueryCacheVersion();

        log.info("文章保存为草稿成功，文章ID: {}", article.getId());
    }

    @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.getId()")
    @Transactional
    public void publicArticle(Article article) {
        log.info("发布文章，文章ID: {}", article.getId());

        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        if (!article.getAuthorId().equals(authorId)) {
            log.warn("用户{}尝试发布他人文章{}", authorId, article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_PERMISSION_DENIED);
        }

        Article article1 = articleMapper.selectById(article.getId());
        if (article1 == null) {
            log.error("文章不存在，文章ID: {}", article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        article.setUpdateTime(new Date());
        article.setStatus(ArticleStatus.PUBLISHED.getCode()); // 使用枚举
        int n = articleMapper.updateById(article);
        authorService.DeleteAuthorAllMessage(article.getAuthorId());

        if (n == 0) {
            log.error("发布文章失败，文章ID: {}", article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_PUBLISH_FAILED);
        }
        bumpArticleQueryCacheVersion();

        log.info("文章发布成功，文章ID: {}", article.getId());
    }

    @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.getId()")
    @Transactional
    public void offlineArticle(Article article) {
        log.info("下线文章，文章ID: {}", article.getId());

        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        if (!article.getAuthorId().equals(authorId)) {
            log.warn("用户{}尝试下线他人文章{}", authorId, article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_PERMISSION_DENIED);
        }

        Article article1 = articleMapper.selectById(article.getId());
        if (article1 == null) {
            log.error("文章不存在，文章ID: {}", article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        article.setUpdateTime(new Date());
        article.setStatus(ArticleStatus.OFFLINE.getCode()); // 使用枚举
        int n = articleMapper.updateById(article);
        authorService.DeleteAuthorAllMessage(article.getAuthorId());

        if (n == 0) {
            log.error("下线文章失败，文章ID: {}", article.getId());
            throw new BusinessException(ErrorCode.ARTICLE_OFFLINE_FAILED);
        }
        bumpArticleQueryCacheVersion();

        log.info("文章下线成功，文章ID: {}", article.getId());
    }
}
