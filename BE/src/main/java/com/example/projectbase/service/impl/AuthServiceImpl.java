package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.RoleConstant;
import com.example.projectbase.constant.UserStatus;
import com.example.projectbase.domain.dto.request.LoginRequestDto;
import com.example.projectbase.domain.dto.request.RegisterRequestDto;
import com.example.projectbase.domain.dto.request.TokenRefreshRequestDto;
import com.example.projectbase.domain.dto.response.CommonResponseDto;
import com.example.projectbase.domain.dto.response.LoginResponseDto;
import com.example.projectbase.domain.dto.response.RegisterResponseDto;
import com.example.projectbase.domain.dto.response.TokenRefreshResponseDto;
import com.example.projectbase.domain.entity.TokenBlacklist;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.entity.UserSession;
import com.example.projectbase.domain.mapper.UserMapper;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.exception.ConflictException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.exception.UnauthorizedException;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.TokenBlacklistRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.repository.UserSessionRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.security.jwt.JwtTokenProvider;
import com.example.projectbase.service.AuthService;
import com.example.projectbase.util.TokenBlacklistUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserSessionRepository userSessionRepository;
  private final TokenBlacklistRepository tokenBlacklistRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final RedisServiceImpl redisService;
  private final ObjectMapper objectMapper;
  private final RedisTemplate redisTemplate;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public RegisterResponseDto register(RegisterRequestDto req) {
      if (userRepository.existsByUsername(req.getUsername())) {
          throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_USERNAME);
      }
      if (userRepository.existsByEmail(req.getEmail())) {
          throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_EXISTS_EMAIL);
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
              .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND, new String[]{RoleConstant.USER}))
      );

      return userMapper.toRegisterDto(userRepository.save(user));
  }


  @Override
  public LoginResponseDto login(LoginRequestDto request, HttpServletRequest httpServletRequest) {
    try {

      List<UserSession> userSessions = userSessionRepository.findAllByUsername(request.getUsernameOrEmail());
      if (!userSessions.isEmpty()) {
          for (UserSession userSession : userSessions) {
              if (userSession.getIsActive()) throw new ConflictException(ErrorMessage.Auth.ERR_ALREADY_LOGGED_IN);
          }
      }

      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, false);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, true);

      User user = userRepository.findById(userPrincipal.getId())
              .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userPrincipal.getId()}));

      String ipAddress = TokenBlacklistUtil.getClientIP(httpServletRequest);

      UserSession userSession = userSessionRepository.findByIpAddressAndUsername(ipAddress, userPrincipal.getUsername());
      if (userSession == null) {
        userSession = new UserSession();
        userSession.setIpAddress(ipAddress);
        userSession.setToken(accessToken);
        userSession.setRefreshToken(refreshToken);
        userSession.setUsername(userPrincipal.getUsername());
        userSession.setUser(user);
        userSession.setIsActive(true);
      }
      else {
        userSession.setIsActive(true);
      }
      userSessionRepository.save(userSession);

      Map<String, Object> dataSession = new HashMap<>();
      dataSession.put("username", userPrincipal.getUsername());
      dataSession.put("status", UserStatus.ONLINE.name());
      dataSession.put("last_activity", LocalDateTime.now());
      String json = objectMapper.writeValueAsString(dataSession);
      redisService.save("username:"+userPrincipal.getUsername()+":session", json);
      return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());
    } catch (InternalAuthenticationServiceException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_USERNAME);
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_PASSWORD);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public TokenRefreshResponseDto refresh(TokenRefreshRequestDto request) {
    logger.info("Processing refresh token request");

    String refreshToken = request.getRefreshToken();
    if (!StringUtils.hasText(refreshToken)) {
      logger.error("Empty refresh token");
      throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
    }

    TokenBlacklist tokenBlacklist = tokenBlacklistRepository.findByToken(refreshToken);
    if (tokenBlacklist != null) {
      throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
    }

    try {

      if (jwtTokenProvider.validateToken(refreshToken)) {
        String username = jwtTokenProvider.extractClaimUsername(refreshToken);
        UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        String newAccessToken = jwtTokenProvider.generateToken(userPrincipal, false);
        UserSession userSession = userSessionRepository.findByRefreshToken(refreshToken);
        if (userSession == null) {
          throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        }
        userSession.setToken(newAccessToken);
        userSessionRepository.save(userSession);
        logger.info("Refresh token successful for user: {}", username);
        return new TokenRefreshResponseDto(newAccessToken, refreshToken);
      }
    } catch (Exception e) {
      logger.error("Error processing refresh token: {}", refreshToken, e);
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_REFRESH_TOKEN);
    }
    return null;
  }

  @Override
  public CommonResponseDto logout(HttpServletRequest request) {
    logger.info("Processing logout request");
    String bearerToken = request.getHeader("Authorization");
    String token = bearerToken.substring(7, bearerToken.length());
    logger.info("Logout token: {}", token);
    UserSession userSession = userSessionRepository.findByToken(token);
    if (userSession == null) {
      throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
    }
    userSession.setIsActive(false);
    userSessionRepository.save(userSession);
    TokenBlacklistUtil.addTokenToBlacklist(token, "Logout access token", tokenBlacklistRepository);
    TokenBlacklistUtil.addTokenToBlacklist(userSession.getRefreshToken(), "Logout refresh token", tokenBlacklistRepository);
    SecurityContextHolder.clearContext();
    return new CommonResponseDto(true, "Logged out successfully");
  }

  @Scheduled(cron = "${cron.deleteExpiredTokenBlacklist}")
  public void deleteExpiredTokenBlacklist() {
    LocalDateTime now = LocalDateTime.now();
    tokenBlacklistRepository.deleteExpiredTokenBlacklist(now);
  }


  @Scheduled(fixedRate = 7 * 60 * 1000L)
  @Transactional
  public void checkUserStatus() {
    logger.info("Checking user status");
    Set<String> keys = redisTemplate.keys("username:*:session");
    List<String> usernamesToDeactivate = new ArrayList<>();
    try {
      for (String key : keys) {
        String username = key.substring(key.indexOf(":") + 1, key.lastIndexOf(":"));
        String json = redisService.get(key);
        Map<String, Object> sessionMap = objectMapper.readValue(json, new TypeReference<>() {});

        LocalDateTime now = LocalDateTime.now();
        String lastActivityStr = (String) sessionMap.get("last_activity");
        LocalDateTime lastActivity = LocalDateTime.parse(lastActivityStr);
        Duration duration = Duration.between(lastActivity, now);

        if (duration.toMinutes() >= 10 && duration.toMinutes() <= 15) {
          sessionMap.put("status", UserStatus.BUSY.name());
        } else if (duration.toMinutes() > 15) {
          sessionMap.put("status", UserStatus.OFFLINE.name());
          usernamesToDeactivate.add(username);
        }

        String updatedJson = objectMapper.writeValueAsString(sessionMap);
        redisService.save("username:" + username + ":session", updatedJson);
      }

      if (!usernamesToDeactivate.isEmpty()) {
        // Gọi một phương thức repository để cập nhật tất cả trong một lần
        userSessionRepository.deactivateUsers(usernamesToDeactivate);
      }

      logger.info("Checked user status");
    } catch (JsonProcessingException ex) {

    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }

}
