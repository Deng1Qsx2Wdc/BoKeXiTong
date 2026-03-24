package com.example.lock;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 可重入的Redis分布式锁
 *
 * 核心原理：
 * 1. 使用ThreadLocal存储当前线程的锁标识和重入次数
 * 2. 每次加锁时检查是否是同一线程，如果是则重入计数+1
 * 3. 解锁时重入计数-1，当计数为0时真正释放锁
 */
@Slf4j
public class ReentrantRedisLock {

    private final Jedis jedis;
    private final String lockKey;
    private final long expireTime; // 锁过期时间（毫秒）

    // 使用ThreadLocal存储当前线程的锁信息
    private static final ThreadLocal<LockInfo> LOCK_INFO_HOLDER = new ThreadLocal<>();

    // Lua脚本：原子性地检查并释放锁
    private static final String UNLOCK_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    public ReentrantRedisLock(Jedis jedis, String lockKey, long expireTime) {
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.expireTime = expireTime;
    }

    /**
     * 尝试获取锁
     */
    public boolean tryLock(long waitTime, TimeUnit unit) throws InterruptedException {
        long waitMillis = unit.toMillis(waitTime);
        long deadline = System.currentTimeMillis() + waitMillis;

        // 检查是否是重入
        LockInfo lockInfo = LOCK_INFO_HOLDER.get();
        if (lockInfo != null) {
            // 同一线程重入，计数+1
            lockInfo.incrementCount();
            log.info("线程 {} 重入锁 {}，当前重入次数: {}",
                Thread.currentThread().getName(), lockKey, lockInfo.getCount());
            return true;
        }

        // 首次获取锁，生成唯一标识
        String lockValue = UUID.randomUUID().toString();

        while (System.currentTimeMillis() < deadline) {
            // 使用SET NX EX命令原子性地设置锁
            SetParams params = new SetParams()
                .nx() // 只在key不存在时设置
                .px(expireTime); // 设置过期时间（毫秒）

            String result = jedis.set(lockKey, lockValue, params);

            if ("OK".equals(result)) {
                // 获取锁成功，保存锁信息到ThreadLocal
                LOCK_INFO_HOLDER.set(new LockInfo(lockValue, 1));
                log.info("线程 {} 获取锁 {} 成功", Thread.currentThread().getName(), lockKey);
                return true;
            }

            // 获取失败，短暂休眠后重试
            Thread.sleep(50);
        }

        log.warn("线程 {} 获取锁 {} 超时", Thread.currentThread().getName(), lockKey);
        return false;
    }

    /**
     * 释放锁
     */
    public void unlock() {
        LockInfo lockInfo = LOCK_INFO_HOLDER.get();

        if (lockInfo == null) {
            throw new IllegalStateException("当前线程未持有锁: " + lockKey);
        }

        // 重入计数-1
        int newCount = lockInfo.decrementCount();

        if (newCount > 0) {
            // 还有重入层级，不真正释放锁
            log.info("线程 {} 释放一层锁 {}，剩余重入次数: {}",
                Thread.currentThread().getName(), lockKey, newCount);
            return;
        }

        // 重入计数为0，真正释放锁
        try {
            // 使用Lua脚本原子性地检查并删除锁
            Object result = jedis.eval(
                UNLOCK_SCRIPT,
                Collections.singletonList(lockKey),
                Collections.singletonList(lockInfo.getLockValue())
            );

            if (Long.valueOf(1).equals(result)) {
                log.info("线程 {} 释放锁 {} 成功", Thread.currentThread().getName(), lockKey);
            } else {
                log.warn("线程 {} 释放锁 {} 失败，锁可能已过期或被其他线程持有",
                    Thread.currentThread().getName(), lockKey);
            }
        } finally {
            // 清理ThreadLocal
            LOCK_INFO_HOLDER.remove();
        }
    }

    /**
     * 锁信息类
     */
    private static class LockInfo {
        private final String lockValue; // 锁的唯一标识
        private int count; // 重入次数

        public LockInfo(String lockValue, int count) {
            this.lockValue = lockValue;
            this.count = count;
        }

        public String getLockValue() {
            return lockValue;
        }

        public int getCount() {
            return count;
        }

        public void incrementCount() {
            count++;
        }

        public int decrementCount() {
            return --count;
        }
    }
}
