package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.dto.request.LoginRequestDto;
import com.example.projectbase.domain.dto.request.TokenRefreshRequestDto;
import com.example.projectbase.domain.dto.response.CommonResponseDto;
import com.example.projectbase.domain.dto.response.LoginResponseDto;
import com.example.projectbase.domain.dto.response.TokenRefreshResponseDto;
import com.example.projectbase.exception.UnauthorizedException;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.security.jwt.JwtTokenProvider;
import com.example.projectbase.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  public LoginResponseDto login(LoginRequestDto request) {
    try {
      log.info("User: " + request.getUsernameOrEmail());
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);
      return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());
    } catch (InternalAuthenticationServiceException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_USERNAME);
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_PASSWORD);
    }
  }


  @Override
  public TokenRefreshResponseDto refresh(TokenRefreshRequestDto request) {
    logger.info("Processing refresh token request");

    String refreshToken = request.getRefreshToken();
    if (!StringUtils.hasText(refreshToken)) {
      logger.error("Empty refresh token");
      throw new UnauthorizedException("Refresh token is required");
    }

    try {
      if (jwtTokenProvider.validateToken(refreshToken) && !jwtTokenProvider.isTokenExpired(refreshToken)) {
        String username = jwtTokenProvider.extractClaimUsername(refreshToken);
        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtTokenProvider.generateToken(userPrincipal, false);
        String newRefreshToken = jwtTokenProvider.generateToken(userPrincipal, true);
        logger.info("Refresh token successful for user: {}", username);
        return new TokenRefreshResponseDto(newAccessToken, newRefreshToken);
      } else {
        logger.error("Invalid or expired refresh token: {}", refreshToken);
        throw new UnauthorizedException("Invalid or expired refresh token");
      }
    } catch (Exception e) {
      logger.error("Error processing refresh token: {}", refreshToken, e);
      throw new UnauthorizedException("Failed to refresh token: " + e.getMessage());
    }
  }

  @Override
  public CommonResponseDto logout(HttpServletRequest request) {
    logger.info("Processing logout request");
    SecurityContextHolder.clearContext();
    return new CommonResponseDto(true, "Logged out successfully");
  }
}