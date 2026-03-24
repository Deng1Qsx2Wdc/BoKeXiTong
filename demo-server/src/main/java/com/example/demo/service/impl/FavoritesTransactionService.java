package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.UserContext;
import com.example.demo.common.enums.InteractionStatus;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.FavoritesMapper;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Favorites;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class FavoritesTransactionService {

    @Autowired
    private FavoritesMapper favoritesMapper;

    @Autowired
    private ArticleMapper articleMapper;

    private int safeFavoritesCount(Integer favorites) {
        return favorites == null ? 0 : favorites;
    }

    @Transactional
    public void unFavorites(Article article) {
        log.info("取消收藏，文章ID: {}", article.getId());

        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        queryWrapper.eq("article_id", article.getId());
        Favorites favorites = favoritesMapper.selectOne(queryWrapper);

        int articleResult = 0;
        int favoritesResult = 0;
        if (favorites != null) {
            favorites.setStatus(InteractionStatus.INACTIVE.getCode());
            favoritesResult = favoritesMapper.updateById(favorites);

            Article currentArticle = articleMapper.selectById(article.getId());
            if (currentArticle == null) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }

            currentArticle.setFavorites(Math.max(0, safeFavoritesCount(currentArticle.getFavorites()) - 1));
            articleResult = articleMapper.updateById(currentArticle);

            if (favoritesResult == 0 || articleResult == 0) {
                log.error("取消收藏失败，文章ID: {}", article.getId());
                throw new BusinessException(ErrorCode.FAVORITES_CANCEL_FAILED);
            }
        }

        log.info("取消收藏成功，文章ID: {}", article.getId());
    }

    @Transactional
    public void upFavorites(Article article) {
        log.info("收藏文章，文章ID: {}", article.getId());

        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        QueryWrapper<Favorites> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        queryWrapper.eq("article_id", article.getId());
        Favorites favorites = favoritesMapper.selectOne(queryWrapper);

        int articleResult = 0;
        int favoritesResult = 0;
        if (favorites != null) {
            favorites.setStatus(InteractionStatus.ACTIVE.getCode());
            favoritesResult = favoritesMapper.updateById(favorites);

            Article currentArticle = articleMapper.selectById(article.getId());
            if (currentArticle == null) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }

            currentArticle.setFavorites(safeFavoritesCount(currentArticle.getFavorites()) + 1);
            articleResult = articleMapper.updateById(currentArticle);
        } else {
            Favorites newFavorites = new Favorites();
            newFavorites.setAuthorId(authorId);
            newFavorites.setArticleId(article.getId());
            newFavorites.setStatus(InteractionStatus.ACTIVE.getCode());
            favoritesResult = favoritesMapper.insert(newFavorites);

            Article currentArticle = articleMapper.selectById(article.getId());
            if (currentArticle == null) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }

            currentArticle.setFavorites(safeFavoritesCount(currentArticle.getFavorites()) + 1);
            articleResult = articleMapper.updateById(currentArticle);
        }

        if (favoritesResult == 0 || articleResult == 0) {
            log.error("收藏失败，文章ID: {}", article.getId());
            throw new BusinessException(ErrorCode.FAVORITES_FAILED);
        }

        log.info("收藏成功，文章ID: {}", article.getId());
    }
}
