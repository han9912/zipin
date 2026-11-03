package io.github.han9912.zipin.opportunity.security;

import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class TelegramUserPrincipal implements UserDetails {
    private final TelegramUser user;

    public TelegramUserPrincipal(TelegramUser user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public String getTelegramId() {
        return user.getTelegramId();
    }

    public String getTelegramUsername() {
        return user.getUsername();
    }

    public String getUsername() {
        return getTelegramUsername() == null ? user.getTelegramId() : getTelegramUsername();
    }

    public String getDisplayName() {
        return user.getDisplayName();
    }

    public String getPictureUrl() {
        return user.getPictureUrl();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
