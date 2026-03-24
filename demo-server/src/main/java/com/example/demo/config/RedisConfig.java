package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);

        //        Redis数据库的本质结构：Redis = Map<String, RedisObject>
        //┌─────────────────────────────────────────┐
        //│  Redis = Map<String, RedisObject>       │
        //│                                         │
        //│  所有的Key都是String类型！                 │
        //│  Value可以是不同的数据结构                  │
        //└─────────────────────────────────────────┘

        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer();


        //Hash类型的Value,这个Value指的是存入到redis中的数据
        redisTemplate.setHashKeySerializer(stringRedisSerializer);//针对的目标是：在存入到redis中的数据的类型是hash 的情况，序列化的目标是这个hash 的key。
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer);//针对的目标是：在存入到redis中的数据的类型是hash 的情况，序列化的目标是这个hash  的value。

        //String类型的Value,这个Value指的是存入到redis中的数据
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer);//针对的是：在存入到redis中的数据的类型是hash 的情况，序列化的目标是这个hash的key。


        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}

