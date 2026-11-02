package io.github.han9912.zipin.opportunity.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@Profile("prod")
public class ProductionAuthReadinessConfiguration {
    @Bean
    public ApplicationRunner validateProductionAuthentication(
            TelegramAuthProperties authProperties,
            SessionCookieProperties cookieProperties
    ) {
        return arguments -> {
            authProperties.validate();
            if (!authProperties.isConfigured()) {
                throw new IllegalStateException("Telegram OIDC must be configured before production startup.");
            }
            if (!cookieProperties.isSecure()) {
                throw new IllegalStateException("Secure authentication cookies are required in production.");
            }
            if (!"https".equalsIgnoreCase(URI.create(authProperties.getRedirectUri()).getScheme())
                    || !"https".equalsIgnoreCase(URI.create(authProperties.getFrontendUrl()).getScheme())) {
                throw new IllegalStateException("Production Telegram callback and frontend URLs must use HTTPS.");
            }
        };
    }
}
