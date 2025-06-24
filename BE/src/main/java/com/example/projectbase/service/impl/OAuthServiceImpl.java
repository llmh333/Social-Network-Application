package com.example.projectbase.service.impl;

import com.example.projectbase.constant.AuthProvider;
import com.example.projectbase.constant.GenderConstant;
import com.example.projectbase.constant.RoleConstant;
import com.example.projectbase.domain.entity.Role;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class OAuthServiceImpl implements OAuthService {

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
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = authToken.getPrincipal().getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            logger.error("No attributes found in OAuth2 token");
            throw new IllegalStateException("OAuth2 attributes cannot be empty");
        }

        logger.debug("Processing OAuth2 login with attributes: {}", attributes);

        String email = extractEmail(attributes);
        Optional<User> userOpt = userRepository.findByEmail(email);

        User user = userOpt.orElseGet(() -> createNewUser(registrationId ,attributes, email));

        UserPrincipal principal = UserPrincipal.create(user);
        principal.setAttributes(attributes);

        logger.info("Successfully processed OAuth2 login for user: {}", email);
        return principal;
    }

    private String extractEmail(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (!StringUtils.hasText(email)) {
            logger.error("Email not found in OAuth2 attributes");
            String name = attributes.get("name").toString();
            email = name + "@facebook.com";
        }
        return email.toLowerCase();
    }

    private User createNewUser(String registrationId, Map<String, Object> attributes, String email) {
        logger.info("Creating new user with email: {}", email);
        logger.info("attributes: {}", attributes.toString());
        String providerId = null;
        String firstName = null;
        String lastName = null;
        String pictureUrl = null;
        GenderConstant gender = GenderConstant.UNKNOWN;
        LocalDate birthday = DEFAULT_DOB;
        if (registrationId.equals("facebook")) {
            providerId = getAttributeOrDefault(attributes, "id", null);
            firstName = getAttributeOrDefault(attributes, "first_name", null);
            lastName = getAttributeOrDefault(attributes, "last_name", null);
            String genderFB = getAttributeOrDefault(attributes, "gender", "unknown");
            try {
                Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
                Map<String, Object> data = (Map<String, Object>) pictureObj.get("data");
                pictureUrl = (String) data.get("url");
            } catch (Exception e) {
                pictureUrl = "";
            }
            if (genderFB.equals(GenderConstant.FEMALE.name())) {
                gender = GenderConstant.FEMALE;
            } else {
                gender = GenderConstant.MALE;
            }
            String birthdayStr = (String) attributes.get("birthday");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            try {
                birthday = LocalDate.parse(birthdayStr, formatter);
            } catch (Exception e) {
                // fallback
            }
        } else if (registrationId.equals("google")) {
            providerId = getAttributeOrDefault(attributes, "sub", null);
            firstName = getAttributeOrDefault(attributes, "given_name", "");
            lastName = getAttributeOrDefault(attributes, "family_name", null);
            pictureUrl = getAttributeOrDefault(attributes, "picture", "");
        }
        if (providerId == null) {
            throw new IllegalArgumentException("Provider ID (sub) is missing in OAuth2 attributes");
        }

        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setPassword("");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDob(birthday);
        user.setProvider(registrationId.equals("facebook") ? AuthProvider.FACEBOOK : AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setGender(gender);
        user.setImageUrl(pictureUrl);
        user.setRole(getDefaultRole());

        try {
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Failed to save new user with email: {}", email, e);
            throw new RuntimeException("Failed to create new user", e);
        }
    }

    private Role getDefaultRole() {
        try {
            return roleRepository.findByName(RoleConstant.USER).get();
        } catch (RuntimeException e) {
            throw new RuntimeException("ROLE_USER not found in DB");
        }
    }

    private String getAttributeOrDefault(Map<String, Object> attributes, String key, String defaultValue) {
        return attributes.get(key) != null ? String.valueOf(attributes.get(key)) : defaultValue;
    }
}