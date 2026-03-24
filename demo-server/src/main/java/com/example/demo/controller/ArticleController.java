package com.example.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.annotation.SystemLog;
import com.example.demo.common.Result;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.ArticleQuery;
import com.example.demo.service.impl.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/article")
@Validated
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    //添加
    @SystemLog(title = "文章模块",businessName = "添加文章")
    @PostMapping("/insert")
    public Result<Object> Insert(@Valid @RequestBody Article article){

        articleService.InsertArticle(article);
        return Result.success();
    }

    //删除
    @SystemLog(title = "文章模块",businessName = "删除文章")
    @PostMapping("/delete")
    public Result<Object> Delete(@RequestBody Article article){

        articleService.DeleteArticle(article);
        return Result.success();
    }

    //查找列表
    @SystemLog(title = "文章模块",businessName = "查找文章列表")
    @PostMapping("/query")
    public Result<Object> Query(@RequestBody ArticleQuery articleQuery){

        Page<Article> page = articleService.QueryArticle(articleQuery);
        return Result.success(page);
    }

    //查找单条
    @SystemLog(title = "文章模块",businessName = "查找单个文章")
    @PostMapping("/queryone")
    public Result<Article> QueryOne(@RequestBody Article articles){

        Article article =  articleService.QueryArticleOne(articles);
        return Result.success(article);
    }

    //修改
    @SystemLog(title = "文章模块",businessName = "修改文章")
    @PostMapping("/update")
    public Result<Object> Update(@Valid @RequestBody Article article) {

        articleService.UpdateArticle(article);
        return Result.success();
    }
    //草稿
    @SystemLog(title = "文章模块",businessName = "修改文章状态为草稿")
    @PostMapping("/draft")
    public Result<Object> Draft(@RequestBody Article article){
        articleService.saveDraft(article);
        return Result.success();
    }
    //发布
    @SystemLog(title = "文章模块",businessName = "修改文章状态为发布")
    @PostMapping("/public")
    public Result<Object> publicArticle(@RequestBody Article article){
        articleService.publicArticle(article);
        return Result.success();
    }
    //下架
    @SystemLog(title = "文章模块",businessName = "修改文章状态为下架")
    @PostMapping("/offline")
    public Result<Object> offline(@RequestBody Article article){
        articleService.offlineArticle(article);
        return Result.success();
    }
}
