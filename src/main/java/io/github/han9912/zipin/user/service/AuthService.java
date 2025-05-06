package io.github.han9912.zipin.user.service;

import io.github.han9912.zipin.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired RedisUtil redisUtil;

    public Long verifyToken(String token) {
        String userId = redisUtil.get("session:" + token);
        if (userId == null) {
            throw new RuntimeException("Token expired");
        }
        return Long.parseLong(userId);
    }
}
