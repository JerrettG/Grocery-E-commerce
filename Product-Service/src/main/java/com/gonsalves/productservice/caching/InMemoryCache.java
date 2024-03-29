package com.gonsalves.productservice.caching;

import com.gonsalves.productservice.service.model.Product;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class InMemoryCache {

    private final Cache<String, Product> productNameCache;
    private final Cache<String, List<Product>> productListCache;

    public InMemoryCache(int expiry, TimeUnit timeUnit, long maximumSize) {
        this.productNameCache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, timeUnit)
                .maximumSize(maximumSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) //uses the available CPU cores for max concurrency
                .build();
        this.productListCache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, TimeUnit.HOURS)
                .maximumSize(maximumSize)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    public Optional<Product> getByProductName(String name) {
        return Optional.ofNullable(productNameCache.getIfPresent(name));
    }
    public Optional<List<Product>> getByCategory(String category) {
        return Optional.ofNullable(productListCache.getIfPresent(category));
    }

    public void evictByProductName(String name) {
        productNameCache.invalidate(name);
    }
    public void evictByCategory(String category) {productListCache.invalidate(category);}

    public void addByProductName(String name, Product value) {
        productNameCache.put(name, value);
    }
    public void addByCategory(String category, List<Product> value ){
        productListCache.put(category, value);
    }

}
