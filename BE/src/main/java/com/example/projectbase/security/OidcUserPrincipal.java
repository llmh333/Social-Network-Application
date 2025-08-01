package com.example.projectbase.security;

import com.example.projectbase.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OidcUserPrincipal extends UserPrincipal implements OidcUser {

    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public OidcUserPrincipal(String id, String firstName, String lastName, String username, String password,
                             Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes,
                             OidcIdToken idToken, OidcUserInfo userInfo) {
        super(id, firstName, lastName, username, password, authorities, attributes);
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return this.idToken.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return this.userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return this.idToken;
    }

    // Static factory method để tạo từ User entity
    public static OidcUserPrincipal create(User user, Map<String, Object> attributes,
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
}
