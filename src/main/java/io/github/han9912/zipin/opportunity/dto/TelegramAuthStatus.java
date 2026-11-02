package io.github.han9912.zipin.opportunity.dto;

public record TelegramAuthStatus(
        boolean enabled,
        boolean authenticated,
        TelegramProfile user
) {
    public record TelegramProfile(String username, String displayName, String pictureUrl) {
    }
}
