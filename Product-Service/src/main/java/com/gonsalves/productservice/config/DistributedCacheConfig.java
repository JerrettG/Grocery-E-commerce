package com.gonsalves.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPoolConfig;
@Configuration
public class DistributedCacheConfig {

    @Bean("JedisPoolConfig")
    public JedisPoolConfig jedisPoolConfig() {
        return new JedisPoolConfig();
    }
}
