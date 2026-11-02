package io.github.han9912.zipin.opportunity.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.opportunity.config.SessionCookieProperties;
import io.github.han9912.zipin.opportunity.config.TelegramAuthProperties;
import io.github.han9912.zipin.opportunity.dto.TelegramAuthStatus;
import io.github.han9912.zipin.opportunity.security.JwtSessionService;
import io.github.han9912.zipin.opportunity.security.TelegramUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class TelegramAuthController {
    private final TelegramAuthProperties authProperties;
    private final SessionCookieProperties cookieProperties;

    public TelegramAuthController(
            TelegramAuthProperties authProperties,
            SessionCookieProperties cookieProperties
    ) {
        this.authProperties = authProperties;
        this.cookieProperties = cookieProperties;
    }

    @GetMapping("/status")
    public Result<TelegramAuthStatus> status(Authentication authentication) {
        if (authentication == null
                || !(authentication.getPrincipal() instanceof TelegramUserPrincipal principal)) {
            return Result.ok(new TelegramAuthStatus(authProperties.isConfigured(), false, null));
        }

        return Result.ok(new TelegramAuthStatus(
                authProperties.isConfigured(),
                true,
                new TelegramAuthStatus.TelegramProfile(
                        principal.getTelegramUsername(),
                        principal.getDisplayName(),
                        principal.getPictureUrl()
                )
        ));
    }

    @GetMapping("/csrf")
    public Result<String> csrfToken(CsrfToken csrfToken) {
        return Result.ok(csrfToken.getToken());
    }

    @PostMapping("/logout")
    public Result<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie
                .from(JwtSessionService.COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build()
                .toString());
        return Result.ok(null);
    }
}
