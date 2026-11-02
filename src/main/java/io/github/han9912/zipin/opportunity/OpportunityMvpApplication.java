package io.github.han9912.zipin.opportunity;

import io.github.han9912.zipin.common.exception.GlobalExceptionHandler;
import io.github.han9912.zipin.opportunity.config.TelegramAuthProperties;
import io.github.han9912.zipin.opportunity.config.SessionCookieProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration(excludeName = {
        "org.redisson.spring.starter.RedissonAutoConfigurationV2",
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
        "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
@ComponentScan(basePackageClasses = OpportunityMvpApplication.class)
@MapperScan("io.github.han9912.zipin.opportunity.mapper")
@Import(GlobalExceptionHandler.class)
@EnableConfigurationProperties({TelegramAuthProperties.class, SessionCookieProperties.class})
public class OpportunityMvpApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpportunityMvpApplication.class, args);
    }
}
