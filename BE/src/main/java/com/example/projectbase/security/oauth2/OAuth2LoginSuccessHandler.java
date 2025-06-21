package com.example.projectbase.security.oauth2;

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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
