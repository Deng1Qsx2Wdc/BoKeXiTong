package com.example.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.annotation.SystemLog;
import com.example.demo.common.Result;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.Author;
import com.example.demo.pojo.entity.Follows;
import com.example.demo.mapper.FollowsMapper;
import com.example.demo.service.impl.FollowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/follows")
public class FollowsController {
    @Autowired
    private FollowsService followsService;

    //获取已关注列表
    @SystemLog(title = "关注模块",businessName = "用户查找自己的关注列表")
    @PostMapping("/followList")
    public Result<Page> FollowList(@RequestBody Author author) {
        Page<Follows> page = followsService.Followser(author);
        return Result.success(page);
    }
    //是否关注此用户
    @SystemLog(title = "关注模块",businessName = "用户查找自己是否关注某个人")
    @PostMapping("/followOne")
    public Result<Follows> FollowList(@RequestBody Follows follows) {
        Follows follow = followsService.FollowOne(follows);
        return Result.success(follow);
    }
    //获取粉丝列表
    @SystemLog(title = "关注模块",businessName = "用户获取自己的粉丝列表")
    @PostMapping("/followerList")
    public Result<List<Follows>> FollowerList(@RequestBody Author author) {
        List<Follows> list = followsService.Followsee(author.getId());
        return Result.success(list);
    }
    //关注
    @SystemLog(title = "关注模块",businessName = "用户关注某个人")
    @PostMapping("/follow")
    public Result<Object> Follow(@RequestBody Follows follows) {
        followsService.Follow(follows);
        return Result.success();
    }
    //取消关注
    @SystemLog(title = "关注模块",businessName = "用户取消关注某个人")
    @PostMapping("/unfollow")
    public Result<Object> Unfollow(@RequestBody Follows follows) {
     followsService.UnFollow(follows);
     return Result.success();
    }
    @GetMapping("/foll")
    public Result<Page> GetFolloingArticle(@RequestParam(defaultValue = "1") Integer Page,
                                           @RequestParam(defaultValue = "10") Integer PageSize) {

        Page<Article> page = followsService.getFollowingArticles(Page,PageSize);
        return Result.success(page);
    }
}
