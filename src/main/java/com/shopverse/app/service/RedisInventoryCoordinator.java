package com.shopverse.app.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Redis stock cache plus token-based distributed locks. Enable with SHOPVERSE_REDIS_ENABLED=true. */
@Component
@ConditionalOnProperty(name = "shopverse.redis.enabled", havingValue = "true")
public class RedisInventoryCoordinator implements InventoryCoordinator {
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final DefaultRedisScript<Long> DECREMENT_IF_AVAILABLE = new DefaultRedisScript<>(
        "local n=tonumber(redis.call('GET', KEYS[1]) or '0'); local q=tonumber(ARGV[1]); " +
        "if n < q then return -1 end; return redis.call('DECRBY', KEYS[1], q)", Long.class);
    private static final DefaultRedisScript<Long> RELEASE_IF_OWNER = new DefaultRedisScript<>(
        "if redis.call('GET', KEYS[1]) == ARGV[1] then return redis.call('DEL', KEYS[1]) else return 0 end", Long.class);
    private final StringRedisTemplate redis;
    private final InventoryProperties properties;
    public RedisInventoryCoordinator(StringRedisTemplate redis, InventoryProperties properties) { this.redis = redis; this.properties = properties; }
    private String stockKey(long id) { return "shopverse:inventory:stock:" + id; }
    private String lockKey(long id) { return "shopverse:inventory:lock:" + id; }
    @Override public int available(long productId) {
        String key = stockKey(productId);
        redis.opsForValue().setIfAbsent(key, String.valueOf(properties.stockFor(productId)));
        String value = redis.opsForValue().get(key);
        return value == null ? 0 : Integer.parseInt(value);
    }
    @Override public void reserve(Map<Long, Integer> quantities) {
        var ids = new ArrayList<>(quantities.keySet()); ids.sort(Comparator.naturalOrder());
        String token = UUID.randomUUID().toString(); var locked = new ArrayList<Long>(); var decremented = new ArrayList<Long>();
        try {
            for (long id : ids) { acquire(id, token); locked.add(id); }
            for (long id : ids) {
                Long result = redis.execute(DECREMENT_IF_AVAILABLE, List.of(stockKey(id)), String.valueOf(quantities.get(id)));
                if (result == null || result < 0) throw new IllegalStateException("商品库存不足，当前仅剩 " + available(id) + " 件");
                decremented.add(id);
            }
        } catch (RuntimeException e) {
            for (long id : decremented) redis.opsForValue().increment(stockKey(id), quantities.get(id));
            throw e;
        } finally {
            for (int i = locked.size() - 1; i >= 0; i--) redis.execute(RELEASE_IF_OWNER, List.of(lockKey(locked.get(i))), token);
        }
    }
    @Override public void release(Map<Long, Integer> quantities) {
        quantities.forEach((id, quantity) -> { available(id); redis.opsForValue().increment(stockKey(id), quantity); });
    }
    private void acquire(long id, String token) {
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        do {
            if (Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(lockKey(id), token, LOCK_TTL))) return;
            try { Thread.sleep(25); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("库存锁获取被中断", e); }
        } while (System.nanoTime() < deadline);
        throw new IllegalStateException("库存繁忙，请稍后重试");
    }
}
