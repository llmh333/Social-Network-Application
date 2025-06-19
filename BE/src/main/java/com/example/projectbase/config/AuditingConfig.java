package com.example.projectbase.config;

import com.example.projectbase.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class AuditingConfig {

  @Bean
  public AuditorAware<String> auditorProvider() {
    return new AuditorAwareImpl();
  }

  private static class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null ||
              !authentication.isAuthenticated() ||
              authentication instanceof AnonymousAuthenticationToken) {
        return Optional.empty();
      }

      Object principal = authentication.getPrincipal();

      // 1) JWT login → UserPrincipal
      if (principal instanceof UserPrincipal) {
        return Optional.ofNullable(((UserPrincipal) principal).getId());
      }

      // 2) OAuth2 login → DefaultOAuth2User
      if (principal instanceof DefaultOAuth2User) {
        DefaultOAuth2User oauthUser = (DefaultOAuth2User) principal;
        // Lấy email (hoặc attribute khác bạn muốn dùng làm auditor)
        String email = oauthUser.getAttribute("email");
        return Optional.ofNullable(email);
      }

      // 3) Thường fallback sang toString()
      return Optional.of(principal.toString());
    }
  }

}