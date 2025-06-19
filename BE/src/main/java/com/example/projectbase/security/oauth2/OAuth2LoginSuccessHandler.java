package com.example.projectbase.security.oauth2;

import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import com.example.projectbase.service.impl.OAuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    final JwtTokenProvider jwtTokenProvider;
    final OAuthServiceImpl oAuthService;

    @Autowired
    public OAuth2LoginSuccessHandler(JwtTokenProvider jwtTokenProvider,
                                     OAuthServiceImpl oAuthService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.oAuthService = oAuthService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        UserPrincipal principal = oAuthService.processOAuthPostLogin(authToken);

        String token = jwtTokenProvider.generateToken(principal, false);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("accessToken", token);
        new ObjectMapper().writeValue(response.getWriter(), tokenResponse);
        response.getWriter().flush();

//        // Redirect với JWT token
//        String redirectUrl = "http://localhost:3000/oauth-success?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
//        response.sendRedirect(redirectUrl);
    }
}
