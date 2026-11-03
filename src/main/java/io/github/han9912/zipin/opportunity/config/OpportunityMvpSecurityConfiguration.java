package io.github.han9912.zipin.opportunity.config;

import io.github.han9912.zipin.opportunity.security.JwtAuthenticationFilter;
import io.github.han9912.zipin.opportunity.security.TelegramAuthFailureHandler;
import io.github.han9912.zipin.opportunity.security.TelegramAuthSuccessHandler;
import io.github.han9912.zipin.opportunity.service.TelegramOidcUserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class OpportunityMvpSecurityConfiguration {
    private static final String LOGIN_BASE_URI = "/api/auth/telegram/login";
    private static final String CALLBACK_BASE_URI = "/api/auth/telegram/callback/*";

    @Bean
    public SecurityFilterChain opportunityMvpFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> registrations,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            TelegramOidcUserService telegramOidcUserService,
            ObjectProvider<TelegramAuthFailureHandler> failureHandlers,
            ObjectProvider<TelegramAuthSuccessHandler> successHandlers,
            SessionCookieProperties cookieProperties
    ) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .secure(cookieProperties.isSecure())
                .sameSite("Lax"));

        http.csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/status", "/api/auth/csrf").permitAll()
                        .requestMatchers("/api/auth/telegram/login/**", "/api/auth/telegram/callback/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/opportunities/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout", "/api/opportunities/**")
                        .authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        ClientRegistrationRepository registrationRepository = registrations.getIfAvailable();
        if (registrationRepository != null) {
            TelegramAuthSuccessHandler successHandler = successHandlers.getIfAvailable();
            if (successHandler == null) {
                throw new IllegalStateException("Telegram OIDC is enabled but its login handler is unavailable.");
            }
            TelegramAuthFailureHandler failureHandler = failureHandlers.getIfAvailable();
            if (failureHandler == null) {
                throw new IllegalStateException("Telegram OIDC is enabled but its failure handler is unavailable.");
            }
            DefaultOAuth2AuthorizationRequestResolver requestResolver =
                    new DefaultOAuth2AuthorizationRequestResolver(registrationRepository, LOGIN_BASE_URI);
            requestResolver.setAuthorizationRequestCustomizer(
                    OAuth2AuthorizationRequestCustomizers.withPkce()
            );
            http.oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(endpoint -> endpoint
                            .baseUri(LOGIN_BASE_URI)
                            .authorizationRequestResolver(requestResolver))
                    .redirectionEndpoint(endpoint -> endpoint.baseUri(CALLBACK_BASE_URI))
                    .userInfoEndpoint(endpoint -> endpoint.oidcUserService(telegramOidcUserService))
                    .successHandler(successHandler)
                    .failureHandler(failureHandler));
        }

        return http.build();
    }
}
