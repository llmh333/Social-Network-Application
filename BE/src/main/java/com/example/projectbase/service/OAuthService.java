package com.example.projectbase.service;

import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.Map;

public interface OAuthService {
    String login(Map<String, Object> attr, Authentication authentication) throws IOException, Exception;
}
