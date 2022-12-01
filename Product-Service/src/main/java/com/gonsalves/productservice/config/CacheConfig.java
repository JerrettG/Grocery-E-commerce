package com.gonsalves.productservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheStore productCache() {
        return new CacheStore(24, TimeUnit.HOURS, 1000);
    }
}
