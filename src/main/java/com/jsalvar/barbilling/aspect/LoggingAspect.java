package com.jsalvar.barbilling.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect     // ← marks this as an Aspect
@Component  // ← Spring manages it
public class LoggingAspect {
    Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(Loggable)") // ← pointcut — intercept @Loggable methods
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // BEFORE
        log.info("→ Calling: {} with args: {}", methodName, args);
        long start = System.currentTimeMillis();

        // THE METHOD
        Object result = joinPoint.proceed();

        // AFTER
        long duration = System.currentTimeMillis() - start;
        log.info("← Completed: {} in {}ms | result: {}", methodName, duration, result);

        return result;

    }
}
