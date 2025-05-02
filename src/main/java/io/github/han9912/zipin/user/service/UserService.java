package io.github.han9912.zipin.user.service;

import io.github.han9912.zipin.user.dto.AuthResponse;
import io.github.han9912.zipin.user.dto.LoginRequest;
import io.github.han9912.zipin.user.dto.RegisterRequest;

public interface UserService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
