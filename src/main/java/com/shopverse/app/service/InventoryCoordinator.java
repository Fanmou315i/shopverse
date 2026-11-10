package com.shopverse.app.service;

import java.util.Map;

/** Coordinates inventory reads and reservations. Implementations must prevent a stock value going below zero. */
public interface InventoryCoordinator {
    int available(long productId);
    void reserve(Map<Long, Integer> quantities);
    void release(Map<Long, Integer> quantities);
}
