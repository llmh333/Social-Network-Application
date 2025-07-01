package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.OidcUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor
@Log4j2
@Service
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            // Gọi service mặc định để lấy OidcUser
            OidcUser oidcUser = super.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oidcUser.getAttributes();

            log.info("OIDC Provider: {}", registrationId);
            log.info("OIDC Attributes: {}", attributes);

            String email = extractEmail(registrationId, attributes);
            log.info("Extracted email: {}", email);

            if (email == null || email.trim().isEmpty()) {
                log.error("Email is null or empty for OIDC provider: {}", registrationId);
                throw new OAuth2AuthenticationException("Email not found in OIDC response");
            }

            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                log.error("User not found with email: {}", email);
                throw new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL, new String[]{email});
            }

            User user = userOptional.get();
            log.info("Found user: {} with ID: {}", user.getUsername(), user.getId());

            // Tạo UserPrincipal implement cả OidcUser và UserPrincipal
            return createOidcUserPrincipal(user, attributes, oidcUser.getIdToken(), oidcUser.getUserInfo());

        } catch (Exception e) {
            log.error("Error in OIDC authentication: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("OIDC authentication failed: " + e.getMessage());
        }
    }

    private OidcUserPrincipal createOidcUserPrincipal(User user, Map<String, Object> attributes,
                                                      OidcIdToken idToken, OidcUserInfo userInfo) {
        List<GrantedAuthority> authorities = new LinkedList<>();
        if (user.getRole() == null) {
            authorities.add(new SimpleGrantedAuthority("USER"));
        } else {
            authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));
        }

        return new OidcUserPrincipal(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                attributes,
                idToken,
                userInfo
        );
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");

        if (StringUtils.hasText(email)) {
            return email.toLowerCase();
        }

        // Fallback logic tương tự như OAuth2Service
        log.warn("Email not found in OIDC attributes for provider: {}", registrationId);

        Object nameObj = attributes.get("name");
        if (nameObj != null && StringUtils.hasText(nameObj.toString())) {
            String name = nameObj.toString().replaceAll("\\s+", "").toLowerCase();
            return name + "@gmail.com";
        }

        Object idObj = attributes.get("sub"); // OIDC sử dụng "sub" thay vì "id"
        if (idObj != null) {
            return "user" + idObj.toString() + "@gmail.com";
        }

        throw new OAuth2AuthenticationException("Unable to extract email from OIDC response");
    }
}
