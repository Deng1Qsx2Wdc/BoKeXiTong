package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.UserContext;
import com.example.demo.common.enums.InteractionStatus;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.ThumbsUpMapper;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Thumbs_up;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ThumbsUpTransactionService {

    @Autowired
    private ThumbsUpMapper thumbsUpMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private AuthorService authorService;

    private int safeThumbsUpCount(Integer thumbsUp) {
        return thumbsUp == null ? 0 : thumbsUp;
    }

    @Transactional
    public void unThumbs(Article article) {
        log.info("取消点赞，文章ID: {}", article.getId());

        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        QueryWrapper<Thumbs_up> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        queryWrapper.eq("article_id", article.getId());
        Thumbs_up thumbsUp = thumbsUpMapper.selectOne(queryWrapper);

        int articleResult = 0;
        int thumbsResult = 0;
        if (thumbsUp != null) {
            thumbsUp.setStatus(InteractionStatus.INACTIVE.getCode());
            thumbsResult = thumbsUpMapper.updateById(thumbsUp);

            Article currentArticle = articleMapper.selectById(article.getId());
            if (currentArticle == null) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }

            currentArticle.setThumbsUp(Math.max(0, safeThumbsUpCount(currentArticle.getThumbsUp()) - 1));
            articleResult = articleMapper.updateById(currentArticle);
            authorService.DeleteAuthorAllMessage(currentArticle.getAuthorId());

            if (thumbsResult == 0 || articleResult == 0) {
                log.error("取消点赞失败，文章ID: {}", article.getId());
                throw new BusinessException(ErrorCode.THUMBS_UP_CANCEL_FAILED);
            }
        }

        log.info("取消点赞成功，文章ID: {}", article.getId());
    }

    @Transactional
    public void upThumbs(Article article) {
        log.info("点赞文章，文章ID: {}", article.getId());

        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }

        QueryWrapper<Thumbs_up> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", authorId);
        queryWrapper.eq("article_id", article.getId());
        Thumbs_up thumbsUp = thumbsUpMapper.selectOne(queryWrapper);

        int articleResult = 0;
        int thumbsResult = 0;
        if (thumbsUp != null) {
            thumbsUp.setStatus(InteractionStatus.ACTIVE.getCode());
            thumbsResult = thumbsUpMapper.updateById(thumbsUp);

            Article currentArticle = articleMapper.selectById(article.getId());
            if (currentArticle == null) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }

            currentArticle.setThumbsUp(safeThumbsUpCount(currentArticle.getThumbsUp()) + 1);
            articleResult = articleMapper.updateById(currentArticle);
            authorService.DeleteAuthorAllMessage(currentArticle.getAuthorId());
        } else {
            Thumbs_up newThumbsUp = new Thumbs_up();
            newThumbsUp.setAuthorId(authorId);
            newThumbsUp.setArticleId(article.getId());
            newThumbsUp.setStatus(InteractionStatus.ACTIVE.getCode());
            thumbsResult = thumbsUpMapper.insert(newThumbsUp);

            Article currentArticle = articleMapper.selectById(article.getId());
            if (currentArticle == null) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }

            currentArticle.setThumbsUp(safeThumbsUpCount(currentArticle.getThumbsUp()) + 1);
            articleResult = articleMapper.updateById(currentArticle);
            authorService.DeleteAuthorAllMessage(currentArticle.getAuthorId());
        }

        if (thumbsResult == 0 || articleResult == 0) {
            log.error("点赞失败，文章ID: {}", article.getId());
            throw new BusinessException(ErrorCode.THUMBS_UP_FAILED);
        }

        log.info("点赞成功，文章ID: {}", article.getId());
    }
}
