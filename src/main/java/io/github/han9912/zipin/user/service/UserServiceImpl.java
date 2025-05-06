package io.github.han9912.zipin.user.service;

import io.github.han9912.zipin.common.util.RedisUtil;
import io.github.han9912.zipin.user.dto.AuthResponse;
import io.github.han9912.zipin.user.dto.LoginRequest;
import io.github.han9912.zipin.user.dto.RegisterRequest;
import io.github.han9912.zipin.user.entity.User;
import io.github.han9912.zipin.user.repository.UserRepository;
import io.github.han9912.zipin.user.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService{
    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private RedisUtil redisUtil;

    public AuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setName(request.name);
        user.setEmail(request.email);
        user.setPassword(new BCryptPasswordEncoder().encode(request.password));
        user.setRole(request.role);
        userRepository.save(user);
        return login(new LoginRequest(request.email, request.password));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!new BCryptPasswordEncoder().matches(request.password, user.getPassword())) {
            throw new RuntimeException("Password incorrect");
        }

        String token = jwtUtil.generateToken(user);
        redisUtil.set("session:" + token, user.getId().toString());

        AuthResponse response = new AuthResponse();
        response.token = token;
        response.role = user.getRole().toString();
        return response;
    }

}
