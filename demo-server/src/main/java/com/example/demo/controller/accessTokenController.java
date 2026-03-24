package com.example.demo.controller;

import com.example.demo.annotation.SystemLog;
import com.example.demo.common.BusinessException;
import com.example.demo.common.ErrorCode;
import com.example.demo.common.Result;
import com.example.demo.pojo.entity.AuthResponse;
import com.example.demo.service.impl.RefreshTokenService;
import com.example.demo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/jwt")
public class accessTokenController {

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String TOKEN_TYPE = "Bearer";

    @SystemLog(title = "JWT模块",businessName = "用户获取AccessToken")
    @PostMapping("/accesstoken")
    public Result<Object> Refresh(@RequestBody String refreshToken) {
        if (!refreshToken.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.TOKEN_TYPE_ERROR);
        }

        String actualRefreshToken = refreshToken.substring(BEARER_PREFIX.length());
        if (!JwtUtils.checkTokenType(actualRefreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_TYPE_ERROR);
        }

        Long authorId = JwtUtils.getUserIdFromToken(actualRefreshToken);
        boolean result = refreshTokenService.validateRefreshToken(authorId, actualRefreshToken);
        if (!result) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String accessToken = JwtUtils.createAccessToken(Long.toString(authorId));
        AuthResponse authResponse = new AuthResponse(accessToken, actualRefreshToken, TOKEN_TYPE, accessTokenExpiration);
        return Result.success(authResponse);
    }

}
