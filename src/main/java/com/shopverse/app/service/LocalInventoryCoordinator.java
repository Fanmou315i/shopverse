package com.shopverse.app.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Local development fallback. Production uses the Redis coordinator when SHOPVERSE_REDIS_ENABLED=true. */
@Component
@ConditionalOnProperty(name = "shopverse.redis.enabled", havingValue = "false", matchIfMissing = true)
public class LocalInventoryCoordinator implements InventoryCoordinator {
    private final InventoryProperties properties;
    private final ConcurrentHashMap<Long, Integer> stock = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();
    public LocalInventoryCoordinator(InventoryProperties properties) { this.properties = properties; }
    @Override public int available(long productId) { return stock.computeIfAbsent(productId, properties::stockFor); }
    @Override public void reserve(Map<Long, Integer> quantities) {
        var ids = new ArrayList<>(quantities.keySet()); ids.sort(Comparator.naturalOrder());
        var acquired = new ArrayList<ReentrantLock>();
        try {
            for (long id : ids) { var lock = locks.computeIfAbsent(id, ignored -> new ReentrantLock()); lock.lock(); acquired.add(lock); }
            for (long id : ids) if (available(id) < quantities.get(id)) throw new IllegalStateException("商品库存不足，当前仅剩 " + available(id) + " 件");
            for (long id : ids) stock.compute(id, (ignored, current) -> current - quantities.get(id));
        } finally { for (int i = acquired.size() - 1; i >= 0; i--) acquired.get(i).unlock(); }
    }
    @Override public void release(Map<Long, Integer> quantities) {
        quantities.forEach((id, quantity) -> stock.merge(id, quantity, Integer::sum));
    }
}
