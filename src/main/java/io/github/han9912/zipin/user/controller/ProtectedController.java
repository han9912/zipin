package io.github.han9912.zipin.user.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.user.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {
    @Autowired
    AuthService authService;

    @GetMapping("/me")
    public Result<String> whoAmI(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long uid = authService.verifyToken(token);
        return Result.ok("当前用户ID： " + uid);
    }
}
