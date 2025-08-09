package com.example.projectbase.util;

import com.example.projectbase.constant.CommonConstant;
import com.example.projectbase.domain.entity.TokenBlacklist;
import com.example.projectbase.repository.TokenBlacklistRepository;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class TokenBlacklistUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

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
        if (request == null) {
            return "unknown";
        }

        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên
                return ip.split(",")[0];
            }
        }

        return request.getRemoteAddr();
    }

}
