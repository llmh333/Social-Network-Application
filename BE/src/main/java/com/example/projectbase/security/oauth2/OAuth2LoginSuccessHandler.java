package com.example.projectbase.security.oauth2;

import com.example.projectbase.base.RestData;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.entity.UserSession;
import com.example.projectbase.exception.ConflictException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.repository.UserSessionRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.security.jwt.JwtTokenProvider;
import com.example.projectbase.service.impl.OAuthServiceImpl;
import com.example.projectbase.util.BeanUtil;
import com.example.projectbase.util.TokenBlacklistUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthServiceImpl oAuthService;
    private final UserSessionRepository userSessionRepository;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        UserPrincipal userPrincipal = oAuthService.processOAuthPostLogin(authToken);

        List<UserSession> userSessions = userSessionRepository.findAllByUsername(userPrincipal.getUsername());
        if (!userSessions.isEmpty()) {
            for (UserSession userSession : userSessions) {
                if (userSession.getIsActive()) {
                    MessageSource messageSource = BeanUtil.getBean(MessageSource.class);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    String message = messageSource.getMessage(ErrorMessage.Auth.ERR_ALREADY_LOGGED_IN, null, LocaleContextHolder.getLocale());
                    response.getOutputStream().write(new ObjectMapper().writeValueAsBytes(RestData.error(message)));
                    return;
                }
            }
        }
        User user = userRepository.findByUsername(userPrincipal.getUsername())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{userPrincipal.getUsername()}));

        String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
        String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

        String ipAddress = TokenBlacklistUtil.getClientIP(request);

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

        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60*60);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(1440*60);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
        response.setContentType("application/json");
        response.sendRedirect("/api/v1" + UrlConstant.OAUTH2_INFO.OAUTH2_TOKEN_INFO);

    }
}
