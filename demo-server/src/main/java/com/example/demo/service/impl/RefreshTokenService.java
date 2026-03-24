package com.example.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
    //从配置文件中获取到refreshtoken的过期时间，并将其赋值给变量。

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    //设置refreshToken的统一缓存key的前缀，便于操作。

    public void storeRefreshToken(Long authorId, String refreshToken) {
        //将refreshToken写入到缓存中。
        String key = REFRESH_TOKEN_PREFIX + authorId;
        //将key的前缀与用户ID拼接成完整的key。

        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );
        //将用户的refreshToken存入到缓存中，过期时间与refreshToken的过期时间一致，保证同步。

    }

    public boolean validateRefreshToken(Long authorId, String refreshToken) {
        //验证待检验的refreshToken是否是指定用户的。
        //验证用户传过来的refreshToken是否与缓存中存储的一致。
        //判断用户的refreshToken是否存储在缓存中。

        String key = REFRESH_TOKEN_PREFIX + authorId;
        //拼接完整的缓存key。前缀+用户ID。

        String storedToken = redisTemplate.opsForValue().get(key);
        //从缓存取出指定用户的refreshToken。

        return refreshToken.equals(storedToken);//验证是否一致
    }

    public void deleteRefreshToken(Long authorId) {
        //删除缓存中的指定用户的refreshToken。

        String key = REFRESH_TOKEN_PREFIX + authorId;

        redisTemplate.delete(key);
    }
}