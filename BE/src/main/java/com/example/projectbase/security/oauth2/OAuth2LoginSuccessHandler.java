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
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.security.jwt.JwtTokenProvider;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import com.example.projectbase.service.impl.OAuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

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

        UserPrincipal userPrincipal = oAuthService.processOAuthPostLogin(authToken);

        String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
        String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(false);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60*60);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(1440*60);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        response.setContentType("application/json;charset=UTF-8");

        response.sendRedirect("/api/v1"+UrlConstant.OAUTH2_INFO.OAUTH2_TOKEN_INFO);
    }
}
