package com.smit.InsightHub_Backend.Helpers;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHelper {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisHelper(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // Set a value in Redis
    public void setValue(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // Get a value from Redis
    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
}