package com.example.projectbase.service.impl;

import com.example.projectbase.constant.AuthProvider;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.RoleConstant;
import com.example.projectbase.domain.dto.request.FirebaseLoginRequest;
import com.example.projectbase.domain.dto.response.LoginResponseDto;

import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.entity.UserSession;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.exception.UnauthorizedException;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.repository.UserSessionRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.security.jwt.JwtTokenProvider;
import com.example.projectbase.util.TokenBlacklistUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class FirebaseAuthService {

   private final FirebaseAuth firebaseAuth;
   private final UserRepository userRepository;
   private final RoleRepository roleRepository;
   private final JwtTokenProvider jwtTokenProvider;
   private final UserSessionRepository userSessionRepository;
   private final RedisServiceImpl redisService;
   private final ObjectMapper objectMapper;

   @Transactional
   public LoginResponseDto loginWithFirebase(FirebaseLoginRequest request, HttpServletRequest httpRequest) {
      try {
         // 1. Verify ID Token using Firebase Admin SDK
         FirebaseToken decodedToken = firebaseAuth.verifyIdToken(request.getIdToken());
         String uid = decodedToken.getUid();
         String email = decodedToken.getEmail();
         String name = decodedToken.getName();
         String picture = decodedToken.getPicture();

         log.info("Firebase Auth: Verified token for email: {}", email);

         // 2. Find or Create User
         User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(email); // Use email as username for OAuth users
            newUser.setPassword(""); // No password
            newUser.setFirstName(name);
            newUser.setImageUrl(picture);
            newUser.setProvider(AuthProvider.GOOGLE); // Assuming mainly Google for now
            newUser.setProviderId(uid);
            newUser.setRole(roleRepository.findByName(RoleConstant.USER)
                  .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND)));
            return userRepository.save(newUser);
         });

         // 3. Create App Tokens (Access/Refresh)
         UserPrincipal userPrincipal = UserPrincipal.create(user);
         Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null,
               userPrincipal.getAuthorities());
         SecurityContextHolder.getContext().setAuthentication(authentication);

         String accessToken = jwtTokenProvider.generateToken(userPrincipal, false);
         String refreshToken = jwtTokenProvider.generateToken(userPrincipal, true);

         // 4. Create Session
         String ipAddress = TokenBlacklistUtil.getClientIP(httpRequest);
         createOrUpdateSession(user, accessToken, refreshToken, ipAddress, userPrincipal.getUsername());

         return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());

      } catch (FirebaseAuthException e) {
         log.error("Firebase Token Verification Failed", e);
         throw new UnauthorizedException("Invalid Firebase ID Token");
      } catch (Exception e) {
         log.error("Login with Firebase failed", e);
         throw new RuntimeException("Authentication failed");
      }
   }

   private void createOrUpdateSession(User user, String accessToken, String refreshToken, String ipAddress,
         String username) {
      UserSession userSession = new UserSession();
      userSession.setIpAddress(ipAddress);
      userSession.setToken(accessToken);
      userSession.setRefreshToken(refreshToken);
      userSession.setUsername(username);
      userSession.setUser(user);
      userSession.setIsActive(true);
      userSessionRepository.save(userSession);

      try {
         Map<String, Object> dataSession = new HashMap<>();
         dataSession.put("username", username);
         dataSession.put("status", "ONLINE");
         dataSession.put("last_activity", LocalDateTime.now().toString());
         String json = objectMapper.writeValueAsString(dataSession);
         redisService.save("username:" + username + ":session", json);
      } catch (JsonProcessingException e) {
         log.error("Error saving session to Redis", e);
      }
   }
}
