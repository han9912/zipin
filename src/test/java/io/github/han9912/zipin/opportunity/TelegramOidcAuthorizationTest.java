package io.github.han9912.zipin.opportunity;

import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import io.github.han9912.zipin.opportunity.mapper.TelegramUserMapper;
import io.github.han9912.zipin.opportunity.security.JwtSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = OpportunityMvpApplication.class,
        properties = {
                "TELEGRAM_OIDC_ENABLED=true",
                "TELEGRAM_CLIENT_ID=123456789",
                "TELEGRAM_CLIENT_SECRET=local-test-only",
                "TELEGRAM_REDIRECT_URI=http://localhost:5173/api/auth/telegram/callback/telegram",
                "APP_FRONTEND_URL=http://localhost:5173/",
                "ZIPIN_JWT_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class TelegramOidcAuthorizationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TelegramUserMapper telegramUserMapper;

    @Autowired
    private JwtSessionService jwtSessionService;

    @Test
    void loginRedirectUsesTelegramOidcAuthorizationCodeAndPkce() throws Exception {
        mockMvc.perform(get("/api/auth/telegram/login/telegram"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("https://oauth.telegram.org/auth")))
                .andExpect(header().string("Location", containsString("response_type=code")))
                .andExpect(header().string("Location", containsString("code_challenge_method=S256")))
                .andExpect(header().string("Location", containsString("code_challenge=")))
                .andExpect(header().string("Location", containsString("state=")))
                .andExpect(header().string("Location", containsString("nonce=")));
    }

    @Test
    void providerDenialRedirectsToFrontendWithoutExposingProviderDetails() throws Exception {
        mockMvc.perform(get("/api/auth/telegram/callback/telegram")
                        .queryParam("error", "access_denied")
                        .queryParam("error_description", "provider detail")
                        .queryParam("state", "invalid-state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "http://localhost:5173/?auth=telegram-failed"));
    }

    @Test
    void validSessionCookieAuthenticatesAgainstTheTelegramUserRecord() throws Exception {
        TelegramUser user = new TelegramUser();
        user.setTelegramId("555001");
        user.setUsername("oidc_test_user");
        user.setDisplayName("OIDC Test User");
        telegramUserMapper.upsert(user);
        TelegramUser savedUser = telegramUserMapper.findByTelegramId(user.getTelegramId());

        mockMvc.perform(get("/api/auth/status")
                        .cookie(new Cookie(
                                JwtSessionService.COOKIE_NAME,
                                jwtSessionService.issue(savedUser)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andExpect(jsonPath("$.data.user.username").value("oidc_test_user"))
                .andExpect(jsonPath("$.data.user.displayName").value("OIDC Test User"));
    }
}
