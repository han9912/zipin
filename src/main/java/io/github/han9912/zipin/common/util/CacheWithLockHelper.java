package io.github.han9912.zipin.common.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@SuppressWarnings("unchecked")
public class CacheWithLockHelper {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    @Autowired
    public CacheWithLockHelper(RedisTemplate<String, Object> redisTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    public <T> T getWithLogicalLock(String key, Supplier<T> cacheLoader, long ttl) {
        T cache = (T) redisTemplate.opsForValue().get(key);
        if (cache != null) return cache;

        String lockKey = "lock:" + key;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                Thread.sleep(100);
                return (T) redisTemplate.opsForValue().get(key);
            }

            cache = (T) redisTemplate.opsForValue().get(key);
            if (cache != null) return cache;

            T data = cacheLoader.get();

            if (data != null) {
                redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
            }
            return data;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Redis锁等待被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {lock.unlock();}
        }
    }
}
