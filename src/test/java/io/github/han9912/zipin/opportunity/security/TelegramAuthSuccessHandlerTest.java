package io.github.han9912.zipin.opportunity.security;

import io.github.han9912.zipin.opportunity.config.SessionCookieProperties;
import io.github.han9912.zipin.opportunity.config.TelegramAuthProperties;
import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import io.github.han9912.zipin.opportunity.service.TelegramUserService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.io.IOException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelegramAuthSuccessHandlerTest {
    @Test
    void successfulOidcLoginSetsAnHttpOnlySessionCookieAndReturnsToTheFrontend()
            throws IOException, ServletException {
        TelegramAuthProperties authProperties = new TelegramAuthProperties();
        authProperties.setFrontendUrl("http://localhost:5173/");
        authProperties.setJwtSecret(Base64.getEncoder().encodeToString(new byte[32]));
        SessionCookieProperties cookieProperties = new SessionCookieProperties();
        TelegramUserService userService = mock(TelegramUserService.class);
        JwtSessionService sessionService = new JwtSessionService(authProperties);
        TelegramUser user = new TelegramUser();
        user.setId(42L);
        user.setTelegramId(123456789L);
        OidcUser oidcUser = mock(OidcUser.class);
        when(userService.upsertFromOidc(oidcUser)).thenReturn(user);

        TelegramAuthSuccessHandler handler = new TelegramAuthSuccessHandler(
                userService,
                sessionService,
                authProperties,
                cookieProperties
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(
                new MockHttpServletRequest(),
                response,
                new UsernamePasswordAuthenticationToken(
                        oidcUser,
                        null,
                        AuthorityUtils.createAuthorityList("ROLE_USER")
                )
        );

        assertEquals(302, response.getStatus());
        assertEquals("http://localhost:5173/", response.getRedirectedUrl());
        String cookie = response.getHeader("Set-Cookie");
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("SameSite=Lax"));
        assertTrue(cookie.contains("Path=/"));
        Matcher tokenMatcher = Pattern.compile("zipin_session=([^;]+)").matcher(cookie);
        assertTrue(tokenMatcher.find());
        assertEquals(42L, sessionService.parseUserId(tokenMatcher.group(1)));
        verify(userService).upsertFromOidc(oidcUser);
    }
}
