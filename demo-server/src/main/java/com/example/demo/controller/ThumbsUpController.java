package com.example.demo.controller;

import com.example.demo.annotation.SystemLog;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Thumbs_up;
import com.example.demo.service.impl.ThumbsUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/thumbs_up")
public class ThumbsUpController {
    @Autowired
    private ThumbsUpService thumbsUpService;
    @SystemLog(title = "点赞模块",businessName = "用户点赞文章/取消点赞文章")
    @PostMapping("/thumbs")
    public Result<Object> Thumbs(@RequestBody Article article){
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        thumbsUpService.Thumbs(article, authorId);
        return Result.success();
    }
    @SystemLog(title = "点赞模块",businessName = "获取点赞某个文章的列表")
    @PostMapping("/thumbs_author_list")
    public Result<Object> getThumbsUpAuthorList(@RequestBody Article article){
        Set<Object> thumbsList = thumbsUpService.getThumbsUpAuthorList(article);
        return Result.success(thumbsList);
    }
    @SystemLog(title = "点赞模块",businessName = "用户获取自己的点赞列表")
    @PostMapping("/author_thumbs_list")
    public Result<Object> getAuthorThumbsUpList(){
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        Set<Object> thumbsList = thumbsUpService.getAuthorThumbsUpList(authorId);
        return Result.success(thumbsList);
    }

}
