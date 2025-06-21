package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.RoleConstant;
import com.example.projectbase.domain.dto.request.LoginRequestDto;
import com.example.projectbase.domain.dto.request.RegisterRequestDto;
import com.example.projectbase.domain.dto.request.TokenRefreshRequestDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.response.CommonResponseDto;
import com.example.projectbase.domain.dto.response.LoginResponseDto;
import com.example.projectbase.domain.dto.response.SignUpResponseDto;
import com.example.projectbase.domain.dto.response.TokenRefreshResponseDto;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.exception.UnauthorizedException;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.security.jwt.JwtTokenProvider;
import com.example.projectbase.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public LoginResponseDto register(RegisterRequestDto req) {
    try {
      if (userRepository.existsByUsername(req.getUsername())) {
          throw new RuntimeException("Username đã tồn tại");
      }
      if (userRepository.existsByEmail(req.getEmail())) {
          throw new RuntimeException("Email đã được đăng ký");
      }

      User user = new User();
      user.setUsername(req.getUsername());
      user.setEmail(req.getEmail());
      user.setPassword(passwordEncoder.encode(req.getPassword()));
      user.setFirstName(req.getFirstName());
      user.setLastName(req.getLastName());
      user.setDob(req.getDob());
      user.setGender(req.getGender());
      user.setRole( roleRepository.findByName(RoleConstant.USER)
              .orElseThrow(() -> new RuntimeException("ROLE_USER not found"))
      );
      userRepository.save(user);
      UserPrincipal principal = UserPrincipal.create(user);

      String accessToken = jwtTokenProvider.generateToken(principal, false);
      String refreshToken = jwtTokenProvider.generateToken(principal, true);

      return new LoginResponseDto(
              accessToken,
              refreshToken,
              principal.getId(),
              principal.getAuthorities()
      );
  } catch (Exception ex) {
    logger.error("Register failed", ex);
    throw ex;
  }
  }

  private final UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public LoginResponseDto login(LoginRequestDto request) {
    try {
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, false);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, true);

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

@Override
public SignUpResponseDto signUp(UserCreateDto request) {

  Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
  if (existingUser.isPresent()) {
    return new SignUpResponseDto("User already exists", false);
  }

  User user = new User();
  user.setUsername(request.getUsername());
  user.setFirstName(request.getFirstName());
  user.setLastName(request.getLastName());

  String hashedPassword = passwordEncoder.encode(request.getPassword());
  user.setPassword(hashedPassword);

  user.setCreatedAt(LocalDateTime.now());
  user.setLastModifiedAt(LocalDateTime.now());

  userRepository.save(user);
  return new SignUpResponseDto("User successfully registered", true);
}

}
}
