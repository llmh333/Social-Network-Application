package com.example.projectbase.service.impl;

import com.example.projectbase.constant.AuthProvider;
import com.example.projectbase.constant.RoleConstant;
import com.example.projectbase.domain.entity.Role;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(OAuthServiceImpl.class);
    private static final LocalDate DEFAULT_DOB = LocalDate.of(1970, 1, 1);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public UserPrincipal processOAuthPostLogin(OAuth2AuthenticationToken authToken) {
        if (authToken == null || authToken.getPrincipal() == null) {
            logger.error("Invalid OAuth2 authentication token provided");
            throw new IllegalArgumentException("Authentication token cannot be null");
        }

        Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            logger.error("No attributes found in OAuth2 token");
            throw new IllegalStateException("OAuth2 attributes cannot be empty");
        }

        logger.debug("Processing OAuth2 login with attributes: {}", attributes);

        String email = extractEmail(attributes);
        Optional<User> userOpt = userRepository.findByEmail(email);

        User user = userOpt.orElseGet(() -> createNewUser(attributes, email));

        UserPrincipal principal = UserPrincipal.create(user);
        principal.setAttributes(attributes);

        logger.info("Successfully processed OAuth2 login for user: {}", email);
        return principal;
    }

    private String extractEmail(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (!StringUtils.hasText(email)) {
            logger.error("Email not found in OAuth2 attributes");
            throw new IllegalStateException("Email is required for OAuth2 authentication");
        }
        return email.toLowerCase();
    }

    private User createNewUser(Map<String, Object> attributes, String email) {
        logger.info("Creating new user with email: {}", email);

        String providerId = getAttributeOrDefault(attributes, "sub", null);
        if (providerId == null) {
            throw new IllegalArgumentException("Provider ID (sub) is missing in OAuth2 attributes");
        }

        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(getAttributeOrDefault(attributes, "given_name", ""));
        user.setLastName(getAttributeOrDefault(attributes, "family_name", ""));
        user.setDob(DEFAULT_DOB);
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setImageUrl(getAttributeOrDefault(attributes, "picture", ""));
        user.setRole(getDefaultRole());

        try {
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Failed to save new user with email: {}", email, e);
            throw new RuntimeException("Failed to create new user", e);
        }
    }

    private Role getDefaultRole() {
        return roleRepository.findByName(RoleConstant.USER)
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found in DB"));
    }

    private String getAttributeOrDefault(Map<String, Object> attributes, String key, String defaultValue) {
        return attributes.get(key) != null ? String.valueOf(attributes.get(key)) : defaultValue;
    }
}