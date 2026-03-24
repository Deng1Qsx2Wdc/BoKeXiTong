package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.config.BCrypt;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.AuthorMapper;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.mapper.FavoritesMapper;
import com.example.demo.mapper.ThumbsUpMapper;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Author;
import com.example.demo.pojo.entity.Comment;
import com.example.demo.pojo.entity.Favorites;
import com.example.demo.pojo.entity.Thumbs_up;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class AuthorService {

    @Autowired
    private AuthorMapper authorMapper;

    @Autowired
    private BCrypt bCrypt;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ThumbsUpMapper thumbsUpMapper;

    @Autowired
    private FavoritesMapper favoritesMapper;

    @Autowired
    private Executor asyncExecutor;

    public Author LoginAuthor(String username, String password) {
        log.info("用户登录，用户名: {}", username);

        QueryWrapper<Author> queryAuthor = new QueryWrapper<>();
        queryAuthor.eq("username", username);
        Author author = authorMapper.selectOne(queryAuthor);

        if (author == null) {
            log.warn("用户登录失败，用户不存在: {}", username);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        boolean matched = bCrypt.passwordEncoder().matches(password, author.getPassword());
        if (!matched) {
            log.warn("用户登录失败，密码错误: {}", username);
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }

        log.info("用户登录成功，用户ID: {}", author.getId());
        return author;
    }

    @Transactional
    public void registerAuthor(String username, String password) {
        log.info("用户注册，用户名: {}", username);

        QueryWrapper<Author> queryAuthor = new QueryWrapper<>();
        queryAuthor.eq("username", username);
        Author author = authorMapper.selectOne(queryAuthor);
        if (author != null) {
            log.warn("用户注册失败，用户名已存在: {}", username);
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        String encodedPassword = bCrypt.passwordEncoder().encode(password);
        Author newAuthor = new Author();
        newAuthor.setUsername(username);
        newAuthor.setPassword(encodedPassword);

        int n = authorMapper.insert(newAuthor);
        if (n == 0) {
            log.error("用户注册失败，数据库插入失败: {}", username);
            throw new BusinessException(ErrorCode.USER_REGISTER_FAILED);
        }

        log.info("用户注册成功，用户ID: {}", newAuthor.getId());
    }

    @CacheEvict(value = {CacheConstants.AUTHOR, CacheConstants.AUTHOR_SEARCH_LIST, CacheConstants.AUTHOR_ALL_MESSAGE}, allEntries = true)
    @Transactional
    public void DeleteAuthor(Long authorId) {
        log.info("删除用户，用户ID: {}", authorId);

        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        int articleCount = articleMapper.delete(queryWrapper);
        int authorCount = authorMapper.deleteById(authorId);

        if (authorCount <= 0) {
            log.error("删除用户失败，用户ID: {}", authorId);
            throw new BusinessException(ErrorCode.USER_DELETE_FAILED);
        }

        log.info("删除用户成功，用户ID: {}，同时删除文章数: {}", authorId, articleCount);
    }

    @Cacheable(value = CacheConstants.AUTHOR_SEARCH_LIST, key = "'PageNum='+#PageNum+':PageSize='+#PageSize+':Name='+#Name")
    public Page<Author> QueryAuthorList(Integer PageNum, Integer PageSize, String Name) {
        log.info("查询用户列表，页码: {}，每页大小: {}，用户名: {}", PageNum, PageSize, Name);

        Page<Author> page = new Page<>(PageNum, PageSize);
        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(Name), "username", Name);
        authorMapper.selectPage(page, queryWrapper);

        log.info("查询用户列表成功，共{}条记录", page.getTotal());
        return page;
    }

    @Cacheable(value = CacheConstants.AUTHOR, key = "'username=' + #username")
    public Author QueryAuthorOne(String username) {
        log.info("查询单个用户，用户名: {}", username);

        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Author author = authorMapper.selectOne(queryWrapper);
        if (author == null) {
            log.warn("查询用户失败，用户不存在: {}", username);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        log.info("查询用户成功，用户ID: {}", author.getId());
        return author;
    }

    @Cacheable(value = CacheConstants.AUTHOR, key = "'id=' + #id")
    public Author QueryAuthorById(Long id) {
        log.info("按用户ID查询单个用户，用户ID: {}", id);

        Author author = authorMapper.selectById(id);
        if (author == null) {
            log.warn("按用户ID查询失败，用户不存在: {}", id);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        log.info("按用户ID查询成功，用户名: {}", author.getUsername());
        return author;
    }

    @CacheEvict(value = {CacheConstants.AUTHOR, CacheConstants.AUTHOR_SEARCH_LIST, CacheConstants.AUTHOR_ALL_MESSAGE}, allEntries = true)
    @Transactional
    public void UpdateAuthor(Author author) {
        log.info("更新用户信息，用户ID: {}", author.getId());

        if (StringUtils.hasText(author.getPassword())) {
            author.setPassword(bCrypt.passwordEncoder().encode(author.getPassword()));
        } else {
            author.setPassword(null);
        }

        int n = authorMapper.updateById(author);
        if (n == 0) {
            log.error("更新用户信息失败，用户ID: {}", author.getId());
            throw new BusinessException(ErrorCode.USER_UPDATE_FAILED);
        }

        log.info("更新用户信息成功，用户ID: {}", author.getId());
    }

    @Cacheable(value = CacheConstants.AUTHOR_ALL_MESSAGE, key = "#authorId")
    public List<Object> GetAuthorAllMessage(Long authorId) {
        log.info("获取用户所有信息，用户ID: {}", authorId);

        CompletableFuture<List<Article>> articleFuture = CompletableFuture.supplyAsync(() -> {
            QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("author_id", authorId);
            return articleMapper.selectList(queryWrapper);
        }, asyncExecutor);

        CompletableFuture<List<Thumbs_up>> thumbsUpFuture = CompletableFuture.supplyAsync(() -> {
            QueryWrapper<Thumbs_up> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("author_id", authorId);
            return thumbsUpMapper.selectList(queryWrapper);
        }, asyncExecutor);

        CompletableFuture<List<Favorites>> favoritesFuture = CompletableFuture.supplyAsync(() -> {
            QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("author_id", authorId);
            return favoritesMapper.selectList(queryWrapper);
        }, asyncExecutor);

        CompletableFuture<List<Comment>> commentFuture = CompletableFuture.supplyAsync(() -> {
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("author_id", authorId);
            return commentMapper.selectList(queryWrapper);
        }, asyncExecutor);

        try {
            CompletableFuture.allOf(articleFuture, thumbsUpFuture, favoritesFuture, commentFuture).join();

            List<Article> articleList = articleFuture.get();
            List<Thumbs_up> thumbsUpList = thumbsUpFuture.get();
            List<Favorites> favoritesList = favoritesFuture.get();
            List<Comment> commentList = commentFuture.get();

            List<Object> list = new ArrayList<>();
            list.add(articleList);
            list.add(thumbsUpList);
            list.add(favoritesList);
            list.add(commentList);

            log.info("获取用户所有信息成功，用户ID: {}，文章数: {}，点赞数: {}，收藏数: {}，评论数: {}",
                    authorId, articleList.size(), thumbsUpList.size(), favoritesList.size(), commentList.size());
            return list;
        } catch (InterruptedException | ExecutionException e) {
            log.error("并行查询用户信息失败，用户ID: {}", authorId, e);
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.DATABASE_QUERY_FAILED);
        }
    }

    @CacheEvict(value = CacheConstants.AUTHOR_ALL_MESSAGE, key = "#authorId")
    @Transactional
    public void DeleteAuthorAllMessage(Long authorId) {
        log.debug("清除用户缓存，用户ID: {}", authorId);
    }
}
