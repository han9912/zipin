package io.github.han9912.zipin.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LogAspect {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(* io.github.han9912..service..*(..))")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("调用方法：{}，参数：{}", joinPoint.getSignature(), Arrays.toString(joinPoint.getArgs()));

        Object result = joinPoint.proceed();

        long elapsed = System.currentTimeMillis() - start;
        log.info("返回结果：{}，耗时：{} ms", result, elapsed);

        return result;
    }
}
