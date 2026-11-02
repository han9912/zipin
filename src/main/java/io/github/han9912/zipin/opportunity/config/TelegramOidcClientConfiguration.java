package io.github.han9912.zipin.opportunity.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "zipin.auth.telegram", name = "enabled", havingValue = "true")
public class TelegramOidcClientConfiguration {
    @Bean
    public ClientRegistrationRepository telegramClientRegistrationRepository(
            TelegramAuthProperties properties
    ) {
        properties.validate();

        ClientRegistration telegram = ClientRegistration.withRegistrationId("telegram")
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(properties.getRedirectUri())
                .scope("openid", "profile")
                .authorizationUri("https://oauth.telegram.org/auth")
                .tokenUri("https://oauth.telegram.org/token")
                .jwkSetUri("https://oauth.telegram.org/.well-known/jwks.json")
                .issuerUri("https://oauth.telegram.org")
                .userNameAttributeName("sub")
                .clientName("Telegram")
                .build();

        return new InMemoryClientRegistrationRepository(telegram);
    }
}
