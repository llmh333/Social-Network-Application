package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.UserStatus;
import com.example.projectbase.domain.entity.UserSession;
import com.example.projectbase.exception.UnauthorizedException;
import com.example.projectbase.repository.UserSessionRepository;
import com.example.projectbase.service.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    private final RedisServiceImpl redisService;
    private final UserSessionRepository userSessionRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void updateLastActivity(String ipAddress, String username) throws JsonProcessingException {
        String userSessionRedis = redisService.get("username:" + username + ":session");
        if (userSessionRedis != null) {
            UserSession userSession  = userSessionRepository.findByIpAddressAndUsername(ipAddress, username);
            if (userSession != null) {
                userSession.setIsActive(true);
                userSessionRepository.save(userSession);

                Map<String, Object> sessionMap = objectMapper.readValue(userSessionRedis, new TypeReference<>() {});

                sessionMap.put("last_activity", LocalDateTime.now().toString());
                sessionMap.put("status", UserStatus.ONLINE.name());
                String updatedJson = objectMapper.writeValueAsString(sessionMap);

                redisService.save("username:" + username + ":session", updatedJson);

                log.info("Updated last activity for user '{}' from IP '{}'", username, ipAddress);
            }
            else {
                log.warn("No active session found for user '{}' from IP '{}'. Skipping activity update.", username, ipAddress);
                throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
            }

        }
    }
}
