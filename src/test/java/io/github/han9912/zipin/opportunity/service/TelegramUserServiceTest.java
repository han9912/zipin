package io.github.han9912.zipin.opportunity.service;

import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import io.github.han9912.zipin.opportunity.mapper.TelegramUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TelegramUserServiceTest {
    @Test
    void upsertPreservesNumericOidcSubjectLargerThanLong() {
        String subject = "9223372036854775808";
        TelegramUserMapper mapper = mock(TelegramUserMapper.class);
        OidcUser oidcUser = mock(OidcUser.class);
        TelegramUser storedUser = new TelegramUser();
        storedUser.setId(42L);
        storedUser.setTelegramId(subject);
        when(oidcUser.getSubject()).thenReturn(subject);
        when(oidcUser.getClaimAsString("preferred_username")).thenReturn("zipin_test");
        when(oidcUser.getClaimAsString("name")).thenReturn("Zipin Test");
        when(mapper.findByTelegramId(subject)).thenReturn(storedUser);

        TelegramUser result = new TelegramUserService(mapper).upsertFromOidc(oidcUser);

        assertEquals(subject, result.getTelegramId());
        verify(mapper).upsert(argThat(user -> subject.equals(user.getTelegramId())));
        verify(mapper).findByTelegramId(subject);
    }

    @Test
    void upsertRejectsEmptyOidcSubject() {
        TelegramUserMapper mapper = mock(TelegramUserMapper.class);
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getSubject()).thenReturn(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> new TelegramUserService(mapper).upsertFromOidc(oidcUser)
        );
    }
}
