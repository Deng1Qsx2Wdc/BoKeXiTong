package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.mapper.FavoritesMapper;
import com.example.demo.mapper.ThumbsUpMapper;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.ArticleQuery;
import com.example.demo.pojo.entity.Comment;
import com.example.demo.pojo.entity.Favorites;
import com.example.demo.pojo.entity.Thumbs_up;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class AdminArticleService {
    private static final String ARTICLE_QUERY_CACHE_VERSION_KEY = CacheConstants.ARTICLE_QUERY_CACHE_VERSION;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private FavoritesMapper favoritesMapper;

    @Autowired
    private ThumbsUpMapper thumbsUpMapper;

    private void bumpArticleQueryCacheVersion() {
        try {
            redisTemplate.opsForValue().increment(ARTICLE_QUERY_CACHE_VERSION_KEY);
        } catch (Exception ignored) {
        }
    }

    @Transactional(readOnly = true)
    public Page<Article> queryArticles(ArticleQuery articleQuery) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        applyKeywordFilter(queryWrapper, articleQuery.getKeyword());
        queryWrapper.eq(articleQuery.getCategoryId() != null, "category_id", articleQuery.getCategoryId());
        queryWrapper.eq(articleQuery.getAuthorId() != null, "author_id", articleQuery.getAuthorId());
        queryWrapper.eq(articleQuery.getStatus() != null, "status", articleQuery.getStatus());
        queryWrapper.ge(articleQuery.getStartTime() != null, "create_time", articleQuery.getStartTime());
        queryWrapper.le(articleQuery.getEndTime() != null, "create_time", articleQuery.getEndTime());
        queryWrapper.orderByDesc("create_time");

        Page<Article> page = new Page<>(articleQuery.getPageNum(), articleQuery.getPageSize());
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

    @Transactional(readOnly = true)
    public Article queryArticle(Article article) {
        Article existingArticle = articleMapper.selectById(article.getId());
        if (existingArticle == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        return existingArticle;
    }

    @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.id")
    @Transactional
    public void updateArticle(Article article) {
        Article existingArticle = articleMapper.selectById(article.getId());
        if (existingArticle == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        article.setAuthorId(existingArticle.getAuthorId());
        article.setStatus(article.getStatus() == null ? existingArticle.getStatus() : article.getStatus());
        article.setUpdateTime(new Date());

        int updated = articleMapper.updateById(article);
        authorService.DeleteAuthorAllMessage(existingArticle.getAuthorId());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.ARTICLE_UPDATE_FAILED);
        }

        bumpArticleQueryCacheVersion();
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#articleId"),
            @CacheEvict(value = CacheConstants.AUTHOR_FAVORITES_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.AUTHOR_THUMBSUP_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.THUMBSUP_ARTICLE_AUTHOR_LIST, allEntries = true),
            @CacheEvict(value = CacheConstants.AUTHOR_ALL_MESSAGE, allEntries = true)
    })
    @Transactional
    public void deleteArticle(Long articleId) {
        Article existingArticle = articleMapper.selectById(articleId);
        if (existingArticle == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        deleteRelatedArticleData(articleId);
        int deleted = articleMapper.deleteById(articleId);
        authorService.DeleteAuthorAllMessage(existingArticle.getAuthorId());
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.ARTICLE_DELETE_FAILED);
        }

        bumpArticleQueryCacheVersion();
    }

    @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#articleId")
    @Transactional
    public void updateArticleStatus(Long articleId, Integer requestedStatus, int fallbackStatus) {
        Article existingArticle = articleMapper.selectById(articleId);
        if (existingArticle == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        Article article = new Article();
        article.setId(articleId);
        article.setAuthorId(existingArticle.getAuthorId());
        article.setStatus(requestedStatus == null ? fallbackStatus : requestedStatus);
        article.setUpdateTime(new Date());

        int updated = articleMapper.updateById(article);
        authorService.DeleteAuthorAllMessage(existingArticle.getAuthorId());
        if (updated == 0) {
            throw new BusinessException(ErrorCode.ARTICLE_UPDATE_FAILED);
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
}
