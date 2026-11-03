package io.github.han9912.zipin.opportunity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import io.github.han9912.zipin.opportunity.mapper.OpportunityMapper;
import io.github.han9912.zipin.opportunity.mapper.TelegramUserMapper;
import io.github.han9912.zipin.opportunity.security.TelegramUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = OpportunityMvpApplication.class,
        properties = "zipin.auth.telegram.enabled=false"
)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class OpportunityDatabaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TelegramUserMapper telegramUserMapper;

    @Autowired
    private OpportunityMapper opportunityMapper;

    @Test
    void authStatusReportsDisabledLoginUntilCredentialsAreConfigured() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.authenticated").value(false));

        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void telegramSubjectLargerThanLongPersistsAsAString() {
        String subject = "9223372036854775808";

        TelegramUser user = telegramUser(subject);
        TelegramUser mixedCaseUser = telegramUser("SubjectABC");
        TelegramUser differentCaseUser = telegramUser("subjectabc");

        assertEquals(subject, user.getTelegramId());
        assertEquals("SubjectABC", mixedCaseUser.getTelegramId());
        assertEquals("subjectabc", differentCaseUser.getTelegramId());
        assertNotEquals(mixedCaseUser.getId(), differentCaseUser.getId());
    }

    @Test
    void postWritesToMySqlAndGetReadsTheSavedOpportunity() throws Exception {
        TelegramUser user = telegramUser("123456789");
        mockMvc.perform(post("/api/opportunities")
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                new TelegramUserPrincipal(user),
                                null,
                                new TelegramUserPrincipal(user).getAuthorities()
                        )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "Part-time",
                                  "title": "Weekend event helper",
                                  "content": "Assist with local event setup",
                                  "contact": "contact@example.test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Weekend event helper"));

        mockMvc.perform(get("/api/opportunities")
                        .queryParam("keyword", "local event"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Weekend event helper"))
                .andExpect(jsonPath("$.data[0].content").value("Assist with local event setup"))
                .andExpect(jsonPath("$.data[0].contact").value("contact@example.test"));
        assertEquals(
                user.getId(),
                opportunityMapper.findLatest("local event", 100).get(0).getPublisherId()
        );
    }

    @Test
    void postRejectsBlankRequiredFields() throws Exception {
        TelegramUser user = telegramUser("123456790");
        mockMvc.perform(post("/api/opportunities")
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                new TelegramUserPrincipal(user),
                                null,
                                new TelegramUserPrincipal(user).getAuthorities()
                        )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "测试",
                                  "title": " ",
                                  "content": "有效内容",
                                  "contact": "contact@example.test"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/opportunities")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private TelegramUser telegramUser(String telegramId) {
        TelegramUser user = new TelegramUser();
        user.setTelegramId(telegramId);
        user.setDisplayName("Integration test user");
        telegramUserMapper.upsert(user);
        return telegramUserMapper.findByTelegramId(telegramId);
    }
}
