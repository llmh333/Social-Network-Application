package com.example.projectbase.aop.aspect;

import com.example.projectbase.service.PostCategoryService;
import com.example.projectbase.service.UserSessionService;
import com.example.projectbase.service.impl.PostCategoryServiceImpl;
import com.example.projectbase.service.impl.UserSessionServiceImpl;
import com.example.projectbase.util.TokenBlacklistUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Aspect
@Configuration
@Slf4j
@RequiredArgsConstructor
public class RepositoryAspect {

  private final UserSessionServiceImpl userSessionService;
  private final PostCategoryServiceImpl postCategoryService;

  private static final String CONTROLLER_POINTCUT =
          "execution(* com.example.projectbase.controller.ReactionController.*(..)) || " +
          "execution(* com.example.projectbase.controller.CommentController.*(..)) || " +
          "execution(* com.example.projectbase.controller.ShareController.*(..))";

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

  @Before("execution(* com.example.projectbase.controller.*.*(..))")
  public void updateLastActivity() {
    try {
      log.info("Updating last activity for user");
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
        String userId = authentication.getName();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = TokenBlacklistUtil.getClientIP(request);
        log.info("Updating last activity for user '{}' from IP '{}'", userId, ipAddress);
        userSessionService.updateLastActivity(ipAddress, userId);
      }
    } catch (Exception e) {
      log.error("Failed to update last activity", e);
    }
  }

  @AfterReturning(pointcut = CONTROLLER_POINTCUT)
  public void updateInteractionCount(JoinPoint joinPoint) {
    Long postId = extractPostIdFromJoinPoint(joinPoint);

    if (postId != null) {
      postCategoryService.increaseInteractCategoryCount(postId);
      postCategoryService.updateTrendingCategoryOnRedis(postId);
    } else {
        log.warn("Could not extract postId from method: {}", joinPoint.getSignature().getName());
    }
  }

  private Long extractPostIdFromJoinPoint(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    String[] parameterNames = signature.getParameterNames(); // Lấy danh sách tên của tất cả các tham số
    Object[] args = joinPoint.getArgs();

    for (int i = 0; i < parameterNames.length; i++) {
      // Kiểm tra xem tên tham số có phải là "postId" không
      if ("postId".equals(parameterNames[i])) {
        // Nếu đúng, kiểm tra xem giá trị của nó có phải là Long không
        if (args[i] instanceof Long) {
          return (Long) args[i]; // Tìm thấy, trả về giá trị
        }
      }
    }

    return null;
  }
}
