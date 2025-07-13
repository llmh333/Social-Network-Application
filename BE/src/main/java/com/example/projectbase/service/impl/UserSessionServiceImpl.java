package com.example.projectbase.service.impl;

import com.example.projectbase.constant.UserStatus;
import com.example.projectbase.service.UserSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    private final RedisServiceImpl redisService;
    private final ObjectMapper objectMapper;

    @Override
    public void updateLastActivity(String username) throws JsonProcessingException {
        String userSessionRedis = redisService.get("username:" + username + ":session");
        if (userSessionRedis != null) {
            Map<String, Object> sessionMap = objectMapper.readValue(userSessionRedis, new TypeReference<>() {});

            sessionMap.put("last_activity", LocalDateTime.now().toString());
            sessionMap.put("status", UserStatus.ONLINE.name());
            String updatedJson = objectMapper.writeValueAsString(sessionMap);

            redisService.save("username:" + username + ":session", updatedJson);

            log.info("Updated last activity: {}", updatedJson);
        }
    }
}
