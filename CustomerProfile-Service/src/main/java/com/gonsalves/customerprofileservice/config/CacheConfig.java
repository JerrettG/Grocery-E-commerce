package com.gonsalves.customerprofileservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheStore orderCache() {
        return new CacheStore(30, TimeUnit.MINUTES, 1000);
    }
}
