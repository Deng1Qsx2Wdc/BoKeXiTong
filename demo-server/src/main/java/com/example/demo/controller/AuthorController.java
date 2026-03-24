package com.example.demo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.annotation.SystemLog;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.Result;
import com.example.demo.common.UserContext;
import com.example.demo.pojo.entity.AuthResponse;
import com.example.demo.pojo.entity.Author;
import com.example.demo.pojo.entity.Login;
import com.example.demo.pojo.entity.ValidationGroups;
import com.example.demo.service.impl.AuthorService;
import com.example.demo.service.impl.RefreshTokenService;
import com.example.demo.service.impl.TokenBlacklistService;
import com.example.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("author")
@Validated
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    private static final String BEARER_PREFIX = "Bearer ";

    @SystemLog(title = "用户模块", businessName = "用户登录")
    @PostMapping("/login")
    public Result<Object> Login(@Valid @RequestBody Login login) {
        Author author = authorService.LoginAuthor(login.getName(), login.getPassword());
        String accessToken = JwtUtils.createAccessToken(author.getId().toString());
        String refreshToken = JwtUtils.createRefreshToken(author.getId().toString());

        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken, "Bearer", refreshTokenExpiration);
        refreshTokenService.storeRefreshToken(author.getId(), refreshToken);

        return Result.success(authResponse);
    }

    @PostMapping("/logout")
    public Result<Object> logout(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(BEARER_PREFIX.length());
        Long remaining = JwtUtils.getRemainingTime(accessToken);
        tokenBlacklistService.addToBlacklist(accessToken, remaining);

        Long authorId = UserContext.getThreadLocal();
        refreshTokenService.deleteRefreshToken(authorId);
        return Result.success();
    }

    @SystemLog(title = "用户模块", businessName = "注册用户")
    @PostMapping("/register")
    public Result<Author> Register(@Valid @RequestBody Login login) {
        authorService.registerAuthor(login.getName(), login.getPassword());
        return Result.success();
    }

    @SystemLog(title = "用户模块", businessName = "删除用户")
    @PostMapping("/delete")
    public Result<Object> Delete(@Validated(ValidationGroups.Delete.class) @RequestBody Author author) {
        authorService.DeleteAuthor(author.getId());
        return Result.success();
    }

    @SystemLog(title = "用户模块", businessName = "查询用户列表")
    @GetMapping("/query")
    public Result<Object> Query(
            @RequestParam(defaultValue = "") String Name,
            @RequestParam(defaultValue = "1") Integer PageNum,
            @RequestParam(defaultValue = "10") Integer PageSize
    ) {
        Page<Author> page = authorService.QueryAuthorList(PageNum, PageSize, Name);
        return Result.success(page);
    }

    @SystemLog(title = "用户模块", businessName = "查询单个用户")
    @GetMapping("/queryone")
    public Result<Object> QueryOne(@RequestParam String username) {
        Author author = authorService.QueryAuthorOne(username);
        return Result.success(author);
    }

    @SystemLog(title = "用户模块", businessName = "按用户ID查询用户")
    @GetMapping("/querybyid")
    public Result<Object> QueryById(@RequestParam Long id) {
        Author author = authorService.QueryAuthorById(id);
        return Result.success(author);
    }

    @SystemLog(title = "用户模块", businessName = "修改用户信息")
    @PostMapping("/update")
    public Result<Object> Update(@Validated(ValidationGroups.Update.class) @RequestBody Author author) {
        authorService.UpdateAuthor(author);
        return Result.success();
    }

    @PostMapping("/allmessage")
    @SystemLog(title = "用户模块", businessName = "查询用户的所有信息")
    public Result<Object> getAuthorAllMessage() {
        Long authorId = UserContext.getThreadLocal();
        if (authorId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_LOGIN);
        }
        List<Object> list = authorService.GetAuthorAllMessage(authorId);
        return Result.success(list);
    }
}
