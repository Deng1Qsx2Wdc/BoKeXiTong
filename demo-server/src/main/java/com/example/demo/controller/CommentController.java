package com.example.demo.controller;

import com.example.demo.annotation.SystemLog;
import com.example.demo.common.Result;
import com.example.demo.pojo.entity.Comment;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.service.impl.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/comment")
@Validated
public class CommentController {
    @Autowired
    private CommentService commentService;
    
    @SystemLog(title = "评论模块",businessName = "添加评论")
    @PostMapping("/setComment")
    public Result<Objects> setComment(@Valid @RequestBody Comment comment){
        commentService.InsertComment(comment);
        return Result.success();
    }
    
    @SystemLog(title = "评论模块",businessName = "查找文章评论")
    @PostMapping("/getCommentList")
    public Result<List<Comment>> getCommentList(@RequestBody Comment comment){
        List<Comment> comments = commentService.getCommentList(comment.getArticleId());
        return Result.success(comments);
    }
}
