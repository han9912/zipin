package io.github.han9912.zipin.common.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
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

    private <T> void writeToCache(String key, T data, long ttl) {
        if (data != null) {
            // 随机TTL避免缓存雪崩
            long randomTtl = ttl + ThreadLocalRandom.current().nextLong(0, 300);
            redisTemplate.opsForValue().set(key, data, randomTtl, TimeUnit.SECONDS);
        } else {
            // 缓存空值并设短TTL避免缓存穿透
            redisTemplate.opsForValue().set(key, "", 120, TimeUnit.SECONDS);
        }
    }

    public <T> T getWithLogicalLock(String key, Supplier<T> cacheLoader, long ttl) {
        T cache = (T) redisTemplate.opsForValue().get(key);
        if (cache != null) {
            if ("".equals(cache)) return null;
            return cache;
        }

        String lockKey = "lock:" + key;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                Thread.sleep(100);
                cache = (T) redisTemplate.opsForValue().get(key);
                if (cache != null) {
                    if ("".equals(cache)) return null;
                    return cache;
                }

                // 异步缓存预热
                CompletableFuture.runAsync(() -> {
                    T asyncData = cacheLoader.get();
                    writeToCache(key, asyncData, ttl);
                });
            }

            cache = (T) redisTemplate.opsForValue().get(key);
            if (cache != null) {
                if ("".equals(cache)) return null;
                return cache;
            }

            T data = cacheLoader.get();
            writeToCache(key, data, ttl);
            return data;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Redis锁等待被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {lock.unlock();}
        }
    }
}
