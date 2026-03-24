package com.example.demo.utils;


import com.example.demo.mapper.ArticleMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类 - 基于Redis实现
 *
 * 核心功能：解决分布式环境下的并发控制问题，防止缓存击穿
 */
@Slf4j
@Data
@Component
public class CacheMutexUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 【踩坑点7：释放了别人的锁】
     * 问题：线程A加锁后业务执行时间过长，锁过期自动释放，线程B抢到锁，此时线程A执行完毕直接删除锁，误删了线程B的锁
     * 解决方案：使用Lua脚本保证"验证锁是否是自己的 + 删除锁"这两个操作的原子性
     *
     * Lua脚本逻辑：
     * 1. 先获取锁的值（KEYS[1]是锁的key）
     * 2. 判断锁的值是否等于当前线程的UUID（ARGV[1]）
     * 3. 如果相等，说明是自己的锁，执行删除操作
     * 4. 如果不相等，说明锁已经不是自己的了（可能过期后被别人抢走），返回0不删除
     *
     * 为什么必须用Lua脚本？
     * 如果分开执行：先get判断，再del删除，这两步不是原子的
     * 在判断完是自己的锁后、执行删除前，锁可能过期并被别人抢走，导致误删别人的锁
     *
     * 最终效果：彻底避免误删别人的锁，保证分布式锁的安全性
     */
    private static String script = "if redis.call('get', KEYS[1]) == ARGV[1] then \n" +
            "    return redis.call('del', KEYS[1]) \n" +
            "else \n" +
            "    return 0 \n" +
            "end";

    private static DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

    /**
     * 尝试获取分布式锁
     *
     * 【踩坑点5：加锁和设置过期时间非原子操作导致死锁】
     * 问题：如果先SETNX加锁，再EXPIRE设置过期时间，两步不是原子的
     * 在加锁成功后、设置过期时间前，如果服务宕机，锁永远不会释放，导致死锁
     *
     * 解决方案：使用SET NX EX命令，一条命令同时完成加锁和设置过期时间，保证原子性
     * 对应Redis命令：SET lock:key UUID NX EX 10
     * - NX：只有key不存在时才设置成功（实现互斥）
     * - EX 10：设置过期时间为10秒（防止死锁）
     *
     * 【踩坑点6：锁过期但业务未执行完导致并发问题】
     * 问题：业务执行时间超过锁的过期时间，锁自动释放，其他线程抢到锁，导致并发执行
     * 解决方案：
     * 1. 设置合理的过期时间（本项目设置10秒，根据业务执行时间评估）
     * 2. 使用UUID标识锁的持有者，释放时验证是否是自己的锁
     * 3. 进阶方案：使用Redisson的看门狗机制自动续期（本项目未实现）
     *
     * 最终效果：
     * - 彻底避免死锁问题，即使服务宕机，锁也会在10秒后自动释放
     * - 通过UUID机制避免误删别人的锁
     * - 配合业务层的重试机制，保证高并发下的数据一致性
     *
     * @param lockName 锁的名称（key）
     * @param UUIDS 当前线程的唯一标识（UUID）
     * @param expireTime 锁的过期时间（秒）
     * @return true-加锁成功，false-加锁失败
     */
    public boolean tryLock(String lockName, String UUIDS, Long expireTime) {
        try {
            // 使用setIfAbsent方法，底层执行：SET lock:key UUID NX EX 10
            // 这是一条原子命令，同时完成加锁和设置过期时间
            Boolean isLoxked = redisTemplate.opsForValue().setIfAbsent(lockName, UUIDS, expireTime, TimeUnit.SECONDS);

            return Boolean.TRUE.equals(isLoxked);
        } catch (Exception e) {
            log.error("分布式锁获取异常！", e);
        }
        return false;
    }

    /**
     * 释放分布式锁
     *
     * 使用Lua脚本保证原子性，避免误删别人的锁（详见上方script注释）
     *
     * @param lockName 锁的名称（key）
     * @param UUIDS 当前线程的唯一标识（UUID）
     */
    public void unlock(String lockName, String UUIDS) {
        try {
            // 执行Lua脚本，传入锁的key和当前线程的UUID
            // Collections.singletonList(lockName) 对应 KEYS[1]
            // UUIDS 对应 ARGV[1]
            redisTemplate.execute(redisScript, Collections.singletonList(lockName), UUIDS);
        } catch (Exception e) {
            log.error("释放分布式锁异常！", e);
        }

    }
}
