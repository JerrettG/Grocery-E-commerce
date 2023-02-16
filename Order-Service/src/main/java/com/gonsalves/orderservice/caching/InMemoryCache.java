package com.gonsalves.orderservice.caching;

import com.gonsalves.orderservice.service.model.Order;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InMemoryCache {

    private final com.google.common.cache.Cache<String, List<Order>> cache;

    public InMemoryCache(int expiry, TimeUnit timeUnit, long maximumSize) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, timeUnit)
                .maximumSize(maximumSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) //uses the available CPU cores for max concurrency
                .build();
    }

    public Optional<List<Order>>  getValue(String key) {
        checkNonNull(key);
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    public void invalidate(String key) {
        checkNonNull(key);
        cache.invalidate(key);
    }

    public void setValue(String key, List<Order> value) {
        checkNonNull(key);
        cache.put(key, value);
    }

    private void checkNonNull(String key) {
        Optional.ofNullable(key).orElseThrow(() -> new IllegalArgumentException("Cache cannot accept null key"));
    }

}

