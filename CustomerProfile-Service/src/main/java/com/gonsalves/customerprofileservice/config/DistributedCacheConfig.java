package com.gonsalves.customerprofileservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class DistributedCacheConfig {

    @Bean("JedisPoolConfig")
    public JedisPoolConfig jedisPoolConfig() {
        return new JedisPoolConfig();
    }
}
