package com.gonsalves.orderservice.caching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;

@Component
public class DistributedCache {
    private final JedisPool jedisPool;

    @Autowired
    public DistributedCache(JedisPoolConfig jedisPoolConfig, @Value("${jedis.url}") String redisUrl) {
        this.jedisPool = new JedisPool(jedisPoolConfig, redisUrl, 6379);
    }

    public Optional<String> getValue(String key) {
        checkNonNull(key);
        try (Jedis jedis = jedisPool.getResource()) {
            return Optional.ofNullable(jedis.get(key));
        }
    }

    public void setValue(String key, int seconds, String value) {
        checkNonNull(key);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, seconds, value);
        }
    }


    public void invalidate(String key) {
        checkNonNull(key);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    private void checkNonNull(String key) {
        Optional.ofNullable(key).orElseThrow(() -> new IllegalArgumentException("Cache cannot accept null key"));
    }

}
