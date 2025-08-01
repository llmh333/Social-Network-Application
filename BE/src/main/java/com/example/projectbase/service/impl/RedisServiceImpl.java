package com.example.projectbase.service.impl;

import com.example.projectbase.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key) == null ? null : redisTemplate.opsForValue().get(key).toString();
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

}
