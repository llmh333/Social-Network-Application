package com.example.projectbase.security.oauth2;

import com.example.projectbase.base.RestData;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.security.jwt.JwtTokenProvider;
import com.example.projectbase.service.impl.OAuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    JwtTokenProvider jwtTokenProvider;
    OAuthServiceImpl oAuthService;
    OAuth2AuthorizedClientService authorizedClientService;
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        UserPrincipal principal = oAuthService.processOAuthPostLogin(authToken);

        String accessToken = jwtTokenProvider.generateToken(principal, false);

        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(
                        authToken.getAuthorizedClientRegistrationId(),
                        authToken.getName()
                );

        String refreshToken = null;
        if (client != null && client.getRefreshToken() != null) {
            refreshToken = client.getRefreshToken().getTokenValue();
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("accessToken", accessToken);
        if (refreshToken != null) {
            payload.put("refreshToken", refreshToken);
        }

        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json;charset=UTF-8");
        RestData<Map<String,Object>> rest = new RestData<>(payload);
        objectMapper.writeValue(response.getWriter(), rest);
        response.getWriter().flush();

        // Điều hướng
//         String redirectUrl = "http://localhost:3000/oauth-success?token="
//              + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
//         response.sendRedirect(redirectUrl);
    }
}
