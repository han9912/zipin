package io.github.han9912.zipin.opportunity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zipin.auth.telegram")
public class TelegramAuthProperties {
    private boolean enabled;
    private String clientId = "";
    private String clientSecret = "";
    private String redirectUri = "";
    private String frontendUrl = "http://localhost:5173/";
    private String jwtSecret = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public boolean isConfigured() {
        return enabled
                && hasText(clientId)
                && hasText(clientSecret)
                && hasText(redirectUri)
                && hasText(jwtSecret);
    }

    public void validate() {
        if (!enabled) {
            return;
        }
        if (!hasText(clientId) || !hasText(clientSecret) || !hasText(redirectUri)
                || !hasText(jwtSecret) || !hasText(frontendUrl)) {
            throw new IllegalStateException(
                    "Telegram OIDC requires client ID, client secret, redirect URI, frontend URL, and JWT secret."
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
