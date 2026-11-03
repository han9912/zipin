package io.github.han9912.zipin.opportunity.security;

import io.github.han9912.zipin.opportunity.config.TelegramAuthProperties;
import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtSessionServiceTest {
    @Test
    void issuedTokenContainsTheLocalUserId() {
        JwtSessionService service = new JwtSessionService(properties());
        TelegramUser user = new TelegramUser();
        user.setId(42L);
        user.setTelegramId("123456789");

        String token = service.issue(user);

        assertEquals(42L, service.parseUserId(token));
    }

    @Test
    void tokenWithAlteredSignatureIsRejected() {
        JwtSessionService service = new JwtSessionService(properties());
        TelegramUser user = new TelegramUser();
        user.setId(42L);
        user.setTelegramId("123456789");
        String token = service.issue(user);
        String[] parts = token.split("\\.");
        parts[2] = (parts[2].startsWith("a") ? "b" : "a") + parts[2].substring(1);
        String tamperedToken = String.join(".", parts);

        assertThrows(io.jsonwebtoken.JwtException.class,
                () -> service.parseUserId(tamperedToken));
    }

    private TelegramAuthProperties properties() {
        TelegramAuthProperties properties = new TelegramAuthProperties();
        properties.setJwtSecret(Base64.getEncoder().encodeToString(new byte[32]));
        return properties;
    }
}
