package com.gonsalves.productservice.caching;

import com.gonsalves.productservice.repository.entity.ProductEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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
            System.out.println("Distributed cache is being set");
            jedis.setex(key, seconds, value);
        }
    }

    public boolean invalidate(String key) {
        checkNonNull(key);
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(key) > 0;
        }
    }




    private void checkNonNull(String key) {
        Optional.ofNullable(key).orElseThrow(() -> new IllegalArgumentException("Cache cannot accept null key"));
    }

}
