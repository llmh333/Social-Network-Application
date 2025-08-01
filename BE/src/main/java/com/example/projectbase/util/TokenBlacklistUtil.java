package com.example.projectbase.util;

import com.example.projectbase.constant.CommonConstant;
import com.example.projectbase.domain.entity.TokenBlacklist;
import com.example.projectbase.repository.TokenBlacklistRepository;

import javax.servlet.http.HttpServletRequest;

public class TokenBlacklistUtil {

    public static void addTokenToBlacklist(String token, String reason, TokenBlacklistRepository tokenBlacklistRepository) {
        TokenBlacklist tokenBlacklist = tokenBlacklistRepository.findByToken(token);
        if (tokenBlacklist == null) {
            TokenBlacklist newTokenBlackList = TokenBlacklist.builder()
                    .token(token)
                    .reason(reason)
                    .tokenType(CommonConstant.BEARER_TOKEN)
                    .build();
            tokenBlacklistRepository.save(newTokenBlackList);
        }
    }

    public static boolean isTokenBlacklisted(String token, TokenBlacklistRepository tokenBlacklistRepository) {
        TokenBlacklist tokenBlacklist = tokenBlacklistRepository.findByToken(token);
        return tokenBlacklist != null;
    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0];
        }
        return ip;
    }

}
