package io.github.han9912.zipin.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LogAspect {
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogAspect.class);

    @Before("execution(* io.github.han9912..controller..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("调用方法：{}， 参数：{}", joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));
    }
}
