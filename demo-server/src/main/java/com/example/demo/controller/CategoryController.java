package com.example.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.annotation.SystemLog;
import com.example.demo.common.Result;
import com.example.demo.pojo.entity.ArticleQuery;
import com.example.demo.pojo.entity.Category;
import com.example.demo.pojo.entity.ValidationGroups;
import com.example.demo.service.impl.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/category")
@Validated
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    //添加
    @SystemLog(title = "分类模块",businessName = "添加分类")
    @PostMapping("/insert")
    public Result<Object> Insert(@Valid @RequestBody Category category){

        categoryService.InsertCategory(category);
        return Result.success();
    }

    //删除
    @SystemLog(title = "分类模块",businessName = "删除分类")
    @PostMapping("/delete")
    public Result<Object> Delete(@Validated(ValidationGroups.Delete.class) @RequestBody Category category){

        categoryService.DeleteCategory(category);

        return Result.success();
    }

    //查找列表
    @SystemLog(title = "分类模块",businessName = "查找分类列表")
    @PostMapping("/query")
    public Result<Page<Category>> Query(@Valid @RequestBody ArticleQuery articleQuery){
        Page<Category> page = categoryService.QueryCategory(articleQuery);
        return Result.success(page);
    }

    //查找单条
    @SystemLog(title = "分类模块",businessName = "查找单个分类")
    @GetMapping("/queryone")
    public Result<Category> QueryOne(@RequestParam Long id){

        Category category = categoryService.QueryCategoryOne(id);
        return Result.success(category);
    }

    //修改
    @SystemLog(title = "分类模块",businessName = "修改分类")
    @PostMapping("/update")
    public Result<Object> Update(@Validated(ValidationGroups.Update.class) @RequestBody Category category) {

        categoryService.UpdateCategory(category);

        return Result.success();
    }
}
