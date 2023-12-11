package com.smit.InsightHub_Backend.Helpers;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHelper {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisHelper(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setValue(String key, String value, long expirationInSeconds) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, Duration.ofSeconds(expirationInSeconds));
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}