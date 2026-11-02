package io.github.han9912.zipin.opportunity.security;

import io.github.han9912.zipin.opportunity.config.TelegramAuthProperties;
import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@ConditionalOnProperty(prefix = "zipin.auth.telegram", name = "enabled", havingValue = "true")
public class JwtSessionService {
    public static final Duration SESSION_TTL = Duration.ofHours(12);
    public static final String COOKIE_NAME = "zipin_session";
    private static final String ISSUER = "zipin-opportunity-mvp";

    private final SecretKey signingKey;

    public JwtSessionService(TelegramAuthProperties properties) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(properties.getJwtSecret());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("ZIPIN_JWT_SECRET must be a Base64-encoded 256-bit key.", exception);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("ZIPIN_JWT_SECRET must decode to at least 32 bytes.");
        }
        signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String issue(TelegramUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getId().toString())
                .claim("telegram_id", user.getTelegramId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(SESSION_TTL)))
                .signWith(signingKey)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
