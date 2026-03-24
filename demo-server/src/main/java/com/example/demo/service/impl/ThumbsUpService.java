package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.common.enums.InteractionStatus;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.ThumbsUpMapper;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Thumbs_up;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ThumbsUpService {

    @Autowired
    private ThumbsUpMapper thumbsUpMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ThumbsUpTransactionService thumbsUpTransactionService;

    @Caching(evict = {
            @CacheEvict(value = CacheConstants.AUTHOR_THUMBSUP_LIST, key = "'authorId=' + #authorId"),
            @CacheEvict(value = CacheConstants.THUMBSUP_ARTICLE_AUTHOR_LIST, key = "'articleId=' + #article.id"),
            @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.getId()")
    })
    @Transactional
    public void Thumbs(Article article, Long authorId) {
        if (article == null || article.getId() == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        Article currentArticle = articleMapper.selectById(article.getId());
        if (currentArticle == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        QueryWrapper<Thumbs_up> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        queryWrapper.eq("article_id", article.getId());
        queryWrapper.eq("status", InteractionStatus.ACTIVE.getCode());
        Thumbs_up activeRecord = thumbsUpMapper.selectOne(queryWrapper);

        if (activeRecord != null) {
            thumbsUpTransactionService.unThumbs(article);
        } else {
            thumbsUpTransactionService.upThumbs(article);
        }

        authorService.DeleteAuthorAllMessage(authorId);
    }

    @Cacheable(value = CacheConstants.THUMBSUP_ARTICLE_AUTHOR_LIST, key = "'articleId=' + #article.id")
    @Transactional(readOnly = true)
    public Set<Object> getThumbsUpAuthorList(Article article) {
        log.info("查询文章点赞用户列表，文章ID: {}", article.getId());

        QueryWrapper<Thumbs_up> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", article.getId());
        queryWrapper.eq("status", InteractionStatus.ACTIVE.getCode());
        List<Thumbs_up> thumbsUpList = thumbsUpMapper.selectList(queryWrapper);
        if (thumbsUpList == null || thumbsUpList.isEmpty()) {
            return new HashSet<>();
        }

        return thumbsUpList.stream().map(Thumbs_up::getAuthorId).collect(Collectors.toSet());
    }

    @Cacheable(value = CacheConstants.AUTHOR_THUMBSUP_LIST, key = "'authorId=' + #authorId")
    @Transactional(readOnly = true)
    public Set<Object> getAuthorThumbsUpList(Long authorId) {
        log.info("查询用户点赞列表，用户ID: {}", authorId);

        QueryWrapper<Thumbs_up> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        queryWrapper.eq("status", InteractionStatus.ACTIVE.getCode());
        List<Thumbs_up> thumbsUpSet = thumbsUpMapper.selectList(queryWrapper);
        if (thumbsUpSet == null || thumbsUpSet.isEmpty()) {
            return new HashSet<>();
        }

        return thumbsUpSet.stream().map(Thumbs_up::getArticleId).collect(Collectors.toSet());
    }
}
