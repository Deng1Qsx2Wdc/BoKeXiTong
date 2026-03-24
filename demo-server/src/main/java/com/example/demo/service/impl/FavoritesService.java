package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.common.enums.InteractionStatus;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.FavoritesMapper;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Favorites;
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
public class FavoritesService {

    @Autowired
    private FavoritesMapper favoritesMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private FavoritesTransactionService favoritesTransactionService;

    @Caching(evict = {
            @CacheEvict(value = CacheConstants.AUTHOR_FAVORITES_LIST, key = "'authorId=' + #authorId"),
            @CacheEvict(value = CacheConstants.ARTICLE_DETAIL, key = "#article.getId()")
    })
    @Transactional
    public void favorites(Article article, Long authorId) {
        if (article == null || article.getId() == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        Article currentArticle = articleMapper.selectById(article.getId());
        if (currentArticle == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        queryWrapper.eq("article_id", article.getId());
        queryWrapper.eq("status", InteractionStatus.ACTIVE.getCode());
        Favorites activeRecord = favoritesMapper.selectOne(queryWrapper);

        if (activeRecord != null) {
            favoritesTransactionService.unFavorites(article);
        } else {
            favoritesTransactionService.upFavorites(article);
        }

        authorService.DeleteAuthorAllMessage(authorId);
    }

    @Cacheable(value = CacheConstants.AUTHOR_FAVORITES_LIST, key = "'authorId=' + #authorId")
    @Transactional(readOnly = true)
    public Set<Object> getAuthorFavoritesList(Long authorId) {
        log.info("查询用户收藏列表，用户ID: {}", authorId);

        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        queryWrapper.eq("status", InteractionStatus.ACTIVE.getCode());
        List<Favorites> favoritesSet = favoritesMapper.selectList(queryWrapper);
        if (favoritesSet == null || favoritesSet.isEmpty()) {
            return new HashSet<>();
        }

        return favoritesSet.stream().map(Favorites::getArticleId).collect(Collectors.toSet());
    }
}
