package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.UserContext;
import com.example.demo.common.constants.CacheConstants;
import com.example.demo.config.SensitiveWordUtil;
import com.example.demo.pojo.entity.Comment;
import com.example.demo.mapper.CommentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SensitiveWordUtil sensitiveWordUtil;

    @CacheEvict(value = CacheConstants.COMMENT,key = "articleId=#comment.articleId")
    public void InsertComment(Comment comment) {
        log.info("添加评论，文章ID: {}", comment.getArticleId());

        String content = comment.getContent();
        content = sensitiveWordUtil.filter(content);
        comment.setContent(content);
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        Long articleId = comment.getArticleId();

        if(content==null||content.length()==0){
            log.warn("评论内容为空，文章ID: {}", articleId);
            throw new BusinessException(ErrorCode.COMMENT_CONTENT_EMPTY);
        }
        comment.setAuthorId(authorId);
        comment.setCreateTime(new Date());

        int n = commentMapper.insert(comment);
        if(n == 0){
            log.error("添加评论失败，文章ID: {}", articleId);
            throw new BusinessException(ErrorCode.COMMENT_INSERT_FAILED);
        }
        log.info("添加评论成功，评论ID: {}，文章ID: {}", comment.getId(), articleId);
    }

    @Cacheable(value = CacheConstants.COMMENT,key = "articleId=#articleId")
    public List<Comment> getCommentList(Long articleId) {
        log.info("查询文章评论列表，文章ID: {}", articleId);

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id",articleId);
        queryWrapper.orderByDesc("create_time");
        List<Comment> comments = commentMapper.selectList(queryWrapper);

        if(comments.isEmpty()){
            log.info("文章暂无评论，文章ID: {}", articleId);
            return new LinkedList<>();
        }

        List<Comment> commentList = tree(comments);//获得构造后的评论树
        log.info("查询评论列表成功，文章ID: {}，评论数: {}", articleId, comments.size());
        return commentList;
    }

    private List<Comment> tree(List<Comment> allcomments) {
        // 使用 HashMap 优化，避免 N+1 查询问题
        // 时间复杂度从 O(n²) 降到 O(n)

        // 1. 按 parentId 分组
        Map<Long, List<Comment>> parentIdToChildren = new HashMap<>();
        List<Comment> rootComments = new ArrayList<>();

        for (Comment comment : allcomments) {
            if (comment.getParentId() == null) {
                rootComments.add(comment);
            } else {
                parentIdToChildren
                    .computeIfAbsent(comment.getParentId(), k -> new ArrayList<>())
                    .add(comment);
            }
        }

        // 2. 递归填充子评论
        for (Comment rootComment : rootComments) {
            fillChildren(rootComment, parentIdToChildren);
        }

        return rootComments;
    }

    private void fillChildren(Comment comment, Map<Long, List<Comment>> parentIdToChildren) {
        List<Comment> children = parentIdToChildren.get(comment.getId());
        if (children != null) {
            comment.getChildren().addAll(children);
            // 递归处理子评论
            for (Comment child : children) {
                fillChildren(child, parentIdToChildren);
            }
        }
    }
}
