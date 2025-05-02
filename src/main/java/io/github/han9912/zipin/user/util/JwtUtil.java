package io.github.han9912.zipin.user.util;

import io.github.han9912.zipin.user.entity.User;
import io.jsonwebtoken.Jwts;        // Jwts 是生成和解析 JWT 的主类
import io.jsonwebtoken.SignatureAlgorithm; // 用于指定加密算法
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private final long EXPIRATION_MS = 86400000;

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build().
                parseClaimsJws(token).
                getBody().
                getSubject());
    }
}