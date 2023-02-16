package com.gonsalves.customerprofileservice.caching;

import com.gonsalves.customerprofileservice.service.model.CustomerProfile;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InMemoryCache {

    private final Cache<String, CustomerProfile> cache;

    public InMemoryCache(int expiry, TimeUnit timeUnit, long maximumSize) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, timeUnit)
                .maximumSize(maximumSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) //uses the available CPU cores for max concurrency
                .build();
    }

    public Optional<CustomerProfile> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    public void evict(String key) {
        cache.invalidate(key);
    }

    public void add(String key, CustomerProfile value) {
        cache.put(key, value);
    }

}

