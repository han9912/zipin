package io.github.han9912.zipin.opportunity.security;

import io.github.han9912.zipin.opportunity.config.TelegramAuthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TelegramAuthFailureHandlerTest {
    @Test
    void failedLoginReturnsToFrontendWithASanitizedFailureCode() throws Exception {
        TelegramAuthProperties properties = new TelegramAuthProperties();
        properties.setFrontendUrl("https://jobpost.duckdns.org/");
        TelegramAuthFailureHandler handler = new TelegramAuthFailureHandler(properties);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("provider error must not be exposed")
        );

        assertEquals(302, response.getStatus());
        assertEquals(
                "https://jobpost.duckdns.org/?auth=telegram-failed",
                response.getRedirectedUrl()
        );
        assertFalse(response.getRedirectedUrl().contains("provider error"));
    }
}
