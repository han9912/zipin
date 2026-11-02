package io.github.han9912.zipin.opportunity.security;

import io.github.han9912.zipin.opportunity.config.SessionCookieProperties;
import io.github.han9912.zipin.opportunity.config.TelegramAuthProperties;
import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import io.github.han9912.zipin.opportunity.service.TelegramUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

@Component
@ConditionalOnProperty(prefix = "zipin.auth.telegram", name = "enabled", havingValue = "true")
public class TelegramAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final TelegramUserService telegramUserService;
    private final JwtSessionService sessionService;
    private final TelegramAuthProperties authProperties;
    private final SessionCookieProperties cookieProperties;

    public TelegramAuthSuccessHandler(
            TelegramUserService telegramUserService,
            JwtSessionService sessionService,
            TelegramAuthProperties authProperties,
            SessionCookieProperties cookieProperties
    ) {
        this.telegramUserService = telegramUserService;
        this.sessionService = sessionService;
        this.authProperties = authProperties;
        this.cookieProperties = cookieProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            throw new ServletException("Telegram OIDC did not provide a verified OIDC user.");
        }

        TelegramUser user = telegramUserService.upsertFromOidc(oidcUser);
        String token = sessionService.issue(user);
        ResponseCookie cookie = ResponseCookie.from(JwtSessionService.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(JwtSessionService.SESSION_TTL)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        response.sendRedirect(URI.create(authProperties.getFrontendUrl()).toASCIIString());
    }
}
