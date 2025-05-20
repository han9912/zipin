package io.github.han9912.zipin.user.service;

import io.github.han9912.zipin.common.util.RedisUtil;
import io.github.han9912.zipin.user.dto.AuthResponse;
import io.github.han9912.zipin.user.dto.LoginRequest;
import io.github.han9912.zipin.user.dto.RegisterRequest;
import io.github.han9912.zipin.user.entity.User;
import io.github.han9912.zipin.user.mapper.UserMapper;
import io.github.han9912.zipin.user.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImpl implements UserService{
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired private JwtUtil jwtUtil;
    @Autowired private RedisUtil redisUtil;
    @Autowired UserMapper userMapper;

    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setName(request.name);
        user.setEmail(request.email);
        user.setPassword(new BCryptPasswordEncoder().encode(request.password));
        user.setRole(request.role);
        userMapper.insert(user);
        logger.info("User {} registered", request.email);
        return login(new LoginRequest(request.email, request.password));
    }

    public AuthResponse login(LoginRequest request) {
        User user = Optional.ofNullable(userMapper.findByEmail(request.email))
                .orElseThrow(() -> new RuntimeException("User not found")) ;

        if (!new BCryptPasswordEncoder().matches(request.password, user.getPassword())) {
            logger.warn("Password incorrect for user {}", request.email);
            throw new RuntimeException("Password incorrect");
        }

        String token = jwtUtil.generateToken(user);
        redisUtil.set("session:" + token, user.getId().toString());

        AuthResponse response = new AuthResponse();
        response.token = token;
        response.role = user.getRole().toString();
        logger.info("User {} logged in", request.email);
        return response;
    }

}
