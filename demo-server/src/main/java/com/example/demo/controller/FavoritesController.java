package com.example.demo.controller;

import com.example.demo.annotation.SystemLog;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import com.example.demo.pojo.entity.Article;
import com.example.demo.service.impl.FavoritesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/favorite")
public class FavoritesController {
    @Autowired
    private FavoritesService favoritesService;
    @SystemLog(title = "收藏模块",businessName = "用户点赞/取消点赞")
    @PostMapping("/favorites")
    public Result<Object> favorites(@RequestBody Article article){
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        favoritesService.favorites(article, authorId);
        return Result.success();
    }
    @SystemLog(title = "收藏模块",businessName = "用户查找点赞列表")
    @PostMapping("/author_favorites_list")
    public Result<Object> getAuthorFavoritesList(){
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        Set<Object> thumbsList = favoritesService.getAuthorFavoritesList(authorId);
        return Result.success(thumbsList);
    }

}
