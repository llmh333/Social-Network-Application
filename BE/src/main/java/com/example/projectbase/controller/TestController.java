package com.example.projectbase.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to OAuth2 Test Application");
        response.put("status", "running");
        return response;
    }

    @GetMapping("/api/v1/auth/test")
    public ResponseEntity<Map<String, Object>> testAuth(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal != null) {
            response.put("authenticated", true);
            response.put("name", principal.getAttribute("name"));
            response.put("email", principal.getAttribute("email"));
            response.put("picture", principal.getAttribute("picture"));
            response.put("principal", principal.getName());
        } else {
            response.put("authenticated", false);
            response.put("message", "User not authenticated");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/auth/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        response.put("status", "success");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/protected")
    public ResponseEntity<Map<String, String>> protectedEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a protected endpoint");
        response.put("status", "authenticated");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> loginPage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Login page");
        response.put("googleLoginUrl", "/oauth2/authorization/google");
        return ResponseEntity.ok(response);
    }
}

@Controller
class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError() {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Page not found");
        response.put("message", "The requested endpoint does not exist");
        response.put("availableEndpoints", new String[]{
                "GET /",
                "GET /api/v1/auth/public",
                "GET /api/v1/auth/test",
                "GET /api/v1/protected",
                "GET /oauth2/authorization/google"
        });
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}