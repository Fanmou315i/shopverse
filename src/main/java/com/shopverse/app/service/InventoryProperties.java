package com.shopverse.app.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "shopverse.inventory")
public class InventoryProperties {
    private Map<Long, Integer> initialStock = new LinkedHashMap<>();
    public Map<Long, Integer> getInitialStock() { return initialStock; }
    public void setInitialStock(Map<Long, Integer> initialStock) { this.initialStock = initialStock; }
    public int stockFor(long productId) { return initialStock.getOrDefault(productId, 0); }
}
