package com.example.demo.config;

import com.example.demo.common.constants.CacheConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 设置默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))//5分钟 TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))//key进行string序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))//value进行json序列化
                .disableCachingNullValues();//空值不缓存

        // 为不同缓存设置不同的 TTL，防止内存浪费
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 用户信息 - 30分钟（变化不频繁）
        cacheConfigurations.put(CacheConstants.AUTHOR, createCacheConfig(Duration.ofMinutes(30)));//调用自定义方法，传入自定义过期时间，得到一个缓存配置，当使用的时候，key为指定值的时候，就执行此缓存配置方法
        cacheConfigurations.put(CacheConstants.AUTHOR_SEARCH_LIST, createCacheConfig(Duration.ofMinutes(10)));
        cacheConfigurations.put(CacheConstants.AUTHOR_ALL_MESSAGE, createCacheConfig(Duration.ofMinutes(15)));

        // 文章详情 - 10分钟（可能被编辑）
        cacheConfigurations.put(CacheConstants.ARTICLE_DETAIL, createCacheConfig(Duration.ofMinutes(10)));

        // 分类信息 - 1小时（很少变化）
        cacheConfigurations.put(CacheConstants.CATEGORY, createCacheConfig(Duration.ofHours(1)));
        cacheConfigurations.put(CacheConstants.CATEGORY_ARTICLE_TOTAL, createCacheConfig(Duration.ofMinutes(30)));

        // 评论列表 - 3分钟（会频繁增加）
        cacheConfigurations.put(CacheConstants.COMMENT, createCacheConfig(Duration.ofMinutes(3)));

        // 统计数据 - 15分钟
        cacheConfigurations.put(CacheConstants.DASHBOARD_COUNT, createCacheConfig(Duration.ofMinutes(15)));

        // 交互数据（点赞、收藏、关注）- 5分钟（频繁变化）
        cacheConfigurations.put(CacheConstants.AUTHOR_FAVORITES_LIST, createCacheConfig(Duration.ofMinutes(5)));
        cacheConfigurations.put(CacheConstants.AUTHOR_FOLLOW, createCacheConfig(Duration.ofMinutes(5)));
        cacheConfigurations.put(CacheConstants.THUMBSUP_ARTICLE_AUTHOR_LIST, createCacheConfig(Duration.ofMinutes(5)));
        cacheConfigurations.put(CacheConstants.AUTHOR_THUMBSUP_LIST, createCacheConfig(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)//默认配置
                .withInitialCacheConfigurations(cacheConfigurations)//指定配置
                .build();
    }


    private RedisCacheConfiguration createCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();
    }

}
