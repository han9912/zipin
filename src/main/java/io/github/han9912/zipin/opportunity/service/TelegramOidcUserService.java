package io.github.han9912.zipin.opportunity.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TelegramOidcUserService extends OidcUserService {
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new OidcUserAuthority(userRequest.getIdToken()));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new DefaultOidcUser(authorities, userRequest.getIdToken(), "sub");
    }
}
