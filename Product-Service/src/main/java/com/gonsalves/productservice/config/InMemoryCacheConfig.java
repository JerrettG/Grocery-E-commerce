package com.gonsalves.productservice.config;

import com.gonsalves.productservice.caching.InMemoryCache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class InMemoryCacheConfig {

    @Bean
    public InMemoryCache productCache() {
        return new InMemoryCache(24, TimeUnit.HOURS, 1000);
    }
}
