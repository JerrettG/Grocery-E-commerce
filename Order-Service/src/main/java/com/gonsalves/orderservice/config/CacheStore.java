package com.gonsalves.productservice.config;

import com.gonsalves.productservice.service.model.Product;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CacheStore {

    private Cache<String, Product> productNameCache;
    private Cache<String, List<Product>> productListCache;

    public CacheStore(int expiry, TimeUnit timeUnit, long size) {
        this.productNameCache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, timeUnit)
                .maximumSize(size)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) //uses the available CPU cores for max concurrency
                .build();
        this.productListCache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiry, TimeUnit.HOURS)
                .concurrencyLevel(Runtime.getRuntime().availableProcessors())
                .build();
    }

    public Product getByProductName(String name) {
        return productNameCache.getIfPresent(name);
    }
    public List<Product> getByCategory(String category) {
        return productListCache.getIfPresent(category);
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
