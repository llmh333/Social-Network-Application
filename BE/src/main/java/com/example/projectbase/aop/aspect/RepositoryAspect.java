package com.example.projectbase.aop.aspect;

import com.example.projectbase.service.UserSessionService;
import com.example.projectbase.service.impl.UserSessionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
@Configuration
@Slf4j
@RequiredArgsConstructor
public class RepositoryAspect {

  private final UserSessionServiceImpl userSessionService;

  @Value("${application.repository.query-limit-warning-ms:60}")
  private int executionLimitMs;

  @Around("execution(* com.example.projectbase.repository.*.*(..))")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    Object proceed = joinPoint.proceed();
    long executionTime = System.currentTimeMillis() - start;
    String message = joinPoint.getSignature() + " exec in " + executionTime + " ms";
    if (executionTime >= executionLimitMs) {
      log.warn(message + " : SLOW QUERY");
    }
    return proceed;
  }

  @Around("execution(* com.example.projectbase.controller.*.*(..))")
  public Object updateLastActivity(ProceedingJoinPoint joinPoint) throws Throwable {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated()) {
        String userId = authentication.getName();
        userSessionService.updateLastActivity(userId);
      }
    } catch (Exception e) {
      log.error("Failed to update last activity", e);
    }

    // Đừng quên gọi proceed()
    return joinPoint.proceed();
  }
}
