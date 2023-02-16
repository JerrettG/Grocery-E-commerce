package com.gonsalves.customerprofileservice.config;

import com.gonsalves.customerprofileservice.caching.InMemoryCache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class InMemoryCacheConfig {

    @Bean
    public InMemoryCache orderCache() {
        return new InMemoryCache(30, TimeUnit.MINUTES, 1000);
    }
}
