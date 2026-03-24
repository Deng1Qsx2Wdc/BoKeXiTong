package com.example.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.pojo.entity.Admin;
import com.example.demo.pojo.entity.Article;
import com.example.demo.pojo.entity.ArticleQuery;
import com.example.demo.pojo.entity.Author;
import com.example.demo.pojo.entity.Category;
import com.example.demo.pojo.entity.Login;
import com.example.demo.service.impl.AdminService;
import com.example.demo.service.impl.AdminArticleService;
import com.example.demo.service.impl.AuthorService;
import com.example.demo.service.impl.CategoryService;
import com.example.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AdminArticleService adminArticleService;

    @PostMapping("/login")
    public Result<Map<String, Object>> Login(@RequestBody Login login) {
        Admin admin = adminService.LoginAdmin(login.getName(), login.getPassword());
        Map<String, Object> result = new HashMap<>();
        String token = JwtUtils.createAccessToken(admin.getId().toString());
        result.put("token", token);
        result.put("admin", admin);
        return Result.success(result);
    }

    @PostMapping("/list")
    public Result<Page<Author>> List(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String Name) {
        Page<Author> page = authorService.QueryAuthorList(pageNum, pageSize, Name);
        return Result.success(page);
    }

    @PostMapping("/users/delete")
    public Result<Object> DeleteUser(@RequestBody Author author) {
        authorService.DeleteAuthor(author.getId());
        return Result.success();
    }

    @PostMapping("/categories/query")
    public Result<Page<Category>> QueryCategories(@RequestBody ArticleQuery articleQuery) {
        return Result.success(categoryService.QueryCategory(articleQuery));
    }

    @PostMapping("/categories/insert")
    public Result<Object> InsertCategory(@RequestBody Category category) {
        categoryService.InsertCategory(category);
        return Result.success();
    }

    @PostMapping("/categories/update")
    public Result<Object> UpdateCategory(@RequestBody Category category) {
        categoryService.UpdateCategory(category);
        return Result.success();
    }

    @PostMapping("/categories/delete")
    public Result<Object> DeleteCategory(@RequestBody Category category) {
        categoryService.DeleteCategory(category);
        return Result.success();
    }

    @PostMapping("/articles/query")
    public Result<Page<Article>> QueryArticles(@RequestBody ArticleQuery articleQuery) {
        return Result.success(adminArticleService.queryArticles(articleQuery));
    }

    @PostMapping("/articles/queryone")
    public Result<Article> QueryArticle(@RequestBody Article article) {
        return Result.success(adminArticleService.queryArticle(article));
    }

    @PostMapping("/articles/update")
    public Result<Object> UpdateArticle(@RequestBody Article article) {
        adminArticleService.updateArticle(article);
        return Result.success();
    }

    @PostMapping("/articles/delete")
    public Result<Object> DeleteArticle(@RequestBody Article article) {
        adminArticleService.deleteArticle(article.getId());
        return Result.success();
    }

    @PostMapping("/articles/draft")
    public Result<Object> DraftArticle(@RequestBody Article article) {
        adminArticleService.updateArticleStatus(article.getId(), article.getStatus(), 0);
        return Result.success();
    }

    @PostMapping("/articles/public")
    public Result<Object> PublicArticle(@RequestBody Article article) {
        adminArticleService.updateArticleStatus(article.getId(), article.getStatus(), 1);
        return Result.success();
    }

    @PostMapping("/articles/offline")
    public Result<Object> OfflineArticle(@RequestBody Article article) {
        adminArticleService.updateArticleStatus(article.getId(), article.getStatus(), 2);
        return Result.success();
    }
}
