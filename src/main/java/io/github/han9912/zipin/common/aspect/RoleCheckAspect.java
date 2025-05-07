package io.github.han9912.zipin.common.aspect;

import io.github.han9912.zipin.common.aspect.annotation.CheckRole;
import io.github.han9912.zipin.user.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

@Aspect
@Component
public class RoleCheckAspect {
    @Autowired JwtUtil jwtUtil;

    @Before("@annotation(role)")
    public void check(CheckRole role) {
        HttpServletRequest request =
                ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String token = request.getHeader("Authorization");
        String userRole = jwtUtil.getRoleFromToken(token);

        String requiredRole = role.value().toString();

        if( !userRole.equals(requiredRole)){
            throw new RuntimeException("无权限访问此接口");
        }
    }
}
