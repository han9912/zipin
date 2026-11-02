package io.github.han9912.zipin.opportunity.security;

import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import io.github.han9912.zipin.opportunity.mapper.TelegramUserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final ObjectProvider<JwtSessionService> sessionServiceProvider;
    private final TelegramUserMapper telegramUserMapper;

    public JwtAuthenticationFilter(
            ObjectProvider<JwtSessionService> sessionServiceProvider,
            TelegramUserMapper telegramUserMapper
    ) {
        this.sessionServiceProvider = sessionServiceProvider;
        this.telegramUserMapper = telegramUserMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        JwtSessionService sessionService = sessionServiceProvider.getIfAvailable();
        String token = findSessionCookie(request);
        if (sessionService != null && token != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Long userId = sessionService.parseUserId(token);
                TelegramUser user = telegramUserMapper.findById(userId);
                if (user != null) {
                    TelegramUserPrincipal principal = new TelegramUserPrincipal(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (io.jsonwebtoken.JwtException | IllegalArgumentException exception) {
                logger.debug("Rejected an invalid or expired Zipin session token.");
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String findSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> JwtSessionService.COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
