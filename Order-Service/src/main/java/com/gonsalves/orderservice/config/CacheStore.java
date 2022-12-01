package com.gonsalves.orderservice.config;

import com.gonsalves.orderservice.service.model.Order;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CacheStore {

    private final Cache<String, List<Order>> cache;

    public CacheStore(int expiry, TimeUnit timeUnit, long maximumSize) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, timeUnit)
                .maximumSize(maximumSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) //uses the available CPU cores for max concurrency
                .build();
    }

    public List<Order> get(String key) {
        return cache.getIfPresent(key);
    }

    public void evict(String key) {
        cache.invalidate(key);
    }

    public void add(String key, List<Order> value) {
        cache.put(key, value);
    }

}

