package io.github.han9912.zipin.opportunity.security;

import io.github.han9912.zipin.opportunity.config.TelegramAuthProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@ConditionalOnProperty(prefix = "zipin.auth.telegram", name = "enabled", havingValue = "true")
public class TelegramAuthFailureHandler implements AuthenticationFailureHandler {
    private static final Logger logger = LoggerFactory.getLogger(TelegramAuthFailureHandler.class);
    private static final String FAILURE_CODE = "telegram-failed";

    private final TelegramAuthProperties authProperties;

    public TelegramAuthFailureHandler(TelegramAuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        logger.warn("Telegram OIDC authentication failed ({})", exception.getClass().getSimpleName());
        String redirectUrl = UriComponentsBuilder.fromUriString(authProperties.getFrontendUrl())
                .queryParam("auth", FAILURE_CODE)
                .build()
                .encode()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }
}
