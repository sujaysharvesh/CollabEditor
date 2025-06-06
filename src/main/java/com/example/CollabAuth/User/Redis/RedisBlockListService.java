package com.example.CollabAuth.User.Redis;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisBlockListService {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisBlockListService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void addToBlockList(String token, Long expirationTime) {
        stringRedisTemplate.opsForValue().set(token, "Blocked", expirationTime, TimeUnit.MILLISECONDS);
    }

}
