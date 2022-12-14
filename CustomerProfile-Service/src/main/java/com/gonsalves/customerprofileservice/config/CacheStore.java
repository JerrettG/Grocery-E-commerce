package com.gonsalves.customerprofileservice.config;

import com.gonsalves.customerprofileservice.service.model.CustomerProfile;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class CacheStore {

    private final Cache<String, CustomerProfile> cache;

    public CacheStore(int expiry, TimeUnit timeUnit, long maximumSize) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, timeUnit)
                .maximumSize(maximumSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) //uses the available CPU cores for max concurrency
                .build();
    }

    public CustomerProfile get(String key) {
        return cache.getIfPresent(key);
    }

    public void evict(String key) {
        cache.invalidate(key);
    }

    public void add(String key, CustomerProfile value) {
        cache.put(key, value);
    }

}

