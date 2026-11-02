package io.github.han9912.zipin.opportunity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zipin.auth.cookie")
public class SessionCookieProperties {
    private boolean secure;

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
