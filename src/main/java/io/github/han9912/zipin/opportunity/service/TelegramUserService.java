package io.github.han9912.zipin.opportunity.service;

import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import io.github.han9912.zipin.opportunity.mapper.TelegramUserMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelegramUserService {
    private final TelegramUserMapper telegramUserMapper;

    public TelegramUserService(TelegramUserMapper telegramUserMapper) {
        this.telegramUserMapper = telegramUserMapper;
    }

    @Transactional
    public TelegramUser upsertFromOidc(OidcUser oidcUser) {
        Long telegramId;
        try {
            telegramId = Long.valueOf(oidcUser.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Telegram OIDC subject must be a numeric Telegram user ID.", exception);
        }

        String username = oidcUser.getClaimAsString("preferred_username");
        String displayName = oidcUser.getClaimAsString("name");
        if (displayName == null || displayName.isBlank()) {
            displayName = username == null || username.isBlank() ? "Telegram user" : username;
        }

        TelegramUser user = new TelegramUser();
        user.setTelegramId(telegramId);
        user.setUsername(username);
        user.setDisplayName(displayName);
        user.setPictureUrl(oidcUser.getClaimAsString("picture"));
        telegramUserMapper.upsert(user);

        TelegramUser savedUser = telegramUserMapper.findByTelegramId(telegramId);
        if (savedUser == null) {
            throw new IllegalStateException("Telegram account was saved but could not be read.");
        }
        return savedUser;
    }

    @Transactional(readOnly = true)
    public TelegramUser findById(Long id) {
        return telegramUserMapper.findById(id);
    }
}
