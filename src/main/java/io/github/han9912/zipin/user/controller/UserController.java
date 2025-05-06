package io.github.han9912.zipin.user.controller;

import io.github.han9912.zipin.user.dto.AuthResponse;
import io.github.han9912.zipin.user.dto.LoginRequest;
import io.github.han9912.zipin.user.dto.RegisterRequest;
import io.github.han9912.zipin.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired private UserService userService;


    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
