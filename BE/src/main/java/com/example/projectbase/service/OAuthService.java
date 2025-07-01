package com.example.projectbase.service;

import com.example.projectbase.domain.entity.User;
import com.example.projectbase.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.io.IOException;
import java.util.Map;

public interface OAuthService {
    public UserPrincipal processOAuthPostLogin(OAuth2AuthenticationToken authToken);
    public User createNewUser(String registrationId, Map<String, Object> attributes, String email);
}
