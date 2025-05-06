package io.github.han9912.zipin.common.util;

import io.github.han9912.zipin.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    @Autowired
    AuthService authService;

    public Long resolveUid(String authHeader) {
        return authService.verifyToken(authHeader.replace("Bearer ", ""));
    }
}
