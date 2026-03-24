package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.UserContext;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.common.enums.InteractionStatus;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.mapper.FollowsMapper;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Author;
import com.example.demo.pojo.entity.Follows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FollowsService {

    @Autowired
    private FollowsMapper followsMapper;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ArticleMapper articleMapper;

    @Cacheable(value = CacheConstants.AUTHOR_FOLLOW, key = "#author.id + ':followser:list'")
    @Transactional(readOnly = true)
    public Page<Follows> Followser(Author author) {
        log.info("查询用户关注列表，用户ID: {}", author.getId());

        Page<Follows> page = new Page<>(1, 10);
        QueryWrapper<Follows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", author.getId());
        queryWrapper.eq("status", InteractionStatus.ACTIVE.getCode());
        queryWrapper.orderByDesc("follow_time");
        followsMapper.selectPage(page, queryWrapper);
        if (page == null) {
            log.error("查询关注列表失败，用户ID: {}", author.getId());
            throw new BusinessException(ErrorCode.DATABASE_QUERY_FAILED);
        }
        return page;
    }

    @Transactional(readOnly = true)
    public Follows FollowOne(Follows follows) {
        QueryWrapper<Follows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", follows.getAuthorId());
        queryWrapper.eq("target_id", follows.getTargetId());
        queryWrapper.eq("status", InteractionStatus.ACTIVE.getCode());
        Follows follow = followsMapper.selectOne(queryWrapper);
        if (follow == null) {
            throw new BusinessException(ErrorCode.FOLLOW_NOT_FOUND);
        }
        return follow;
    }

    @Cacheable(value = CacheConstants.AUTHOR_FOLLOW, key = "#authorId + ':followsee:list'")
    @Transactional(readOnly = true)
    public List<Follows> Followsee(Long authorId) {
        log.info("查询用户粉丝列表，用户ID: {}", authorId);

        QueryWrapper<Follows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_id", authorId);
        queryWrapper.eq("status", InteractionStatus.ACTIVE.getCode());
        queryWrapper.orderByDesc("follow_time");
        List<Follows> list = followsMapper.selectList(queryWrapper);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        return list;
    }

    @CacheEvict(value = CacheConstants.AUTHOR_FOLLOW, key = "#follows.authorId + ':followser:list'")
    public void Follow(Follows follows) {
        log.info("关注用户，目标用户ID: {}", follows.getTargetId());

        QueryWrapper<Follows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", follows.getAuthorId());
        queryWrapper.eq("target_id", follows.getTargetId());
        Follows existingFollow = followsMapper.selectOne(queryWrapper);
        if (existingFollow != null) {
            if (InteractionStatus.ACTIVE.getCode().equals(existingFollow.getStatus())) {
                log.warn("已关注该用户，目标用户ID: {}", follows.getTargetId());
                throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
            }

            existingFollow.setStatus(InteractionStatus.ACTIVE.getCode());
            int updated = followsMapper.updateById(existingFollow);
            authorService.DeleteAuthorAllMessage(follows.getAuthorId());
            if (updated == 0) {
                log.error("重新关注用户失败，目标用户ID: {}", follows.getTargetId());
                throw new BusinessException(ErrorCode.FOLLOW_FAILED);
            }
            log.info("重新关注用户成功，目标用户ID: {}", follows.getTargetId());
            return;
        }

        follows.setStatus(InteractionStatus.ACTIVE.getCode());
        int inserted = followsMapper.insert(follows);
        authorService.DeleteAuthorAllMessage(follows.getAuthorId());
        if (inserted == 0) {
            log.error("关注用户失败，目标用户ID: {}", follows.getTargetId());
            throw new BusinessException(ErrorCode.FOLLOW_FAILED);
        }
        log.info("关注用户成功，目标用户ID: {}", follows.getTargetId());
    }

    @CacheEvict(value = CacheConstants.AUTHOR_FOLLOW, key = "#follows.authorId + ':followser:list'")
    public void UnFollow(Follows follows) {
        log.info("取消关注用户，目标用户ID: {}", follows.getTargetId());

        follows.setStatus(InteractionStatus.INACTIVE.getCode());
        QueryWrapper<Follows> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("author_id", follows.getAuthorId());
        queryWrapper.eq("target_id", follows.getTargetId());
        int updated = followsMapper.update(follows, queryWrapper);
        authorService.DeleteAuthorAllMessage(follows.getAuthorId());
        if (updated == 0) {
            log.error("取消关注失败，目标用户ID: {}", follows.getTargetId());
            throw new BusinessException(ErrorCode.FOLLOW_CANCEL_FAILED);
        }
        log.info("取消关注成功，目标用户ID: {}", follows.getTargetId());
    }

    @Transactional(readOnly = true)
    public Page<Article> getFollowingArticles(Integer page, Integer pageSize) {
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        Page<Article> articlePage = new Page<>(page, pageSize);
        articleMapper.getFollowingArticles(articlePage, authorId);
        return articlePage;
    }
}
