package com.example.projectbase.security;

import com.example.projectbase.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

public class UserPrincipal implements UserDetails, OAuth2User {

    private final String id;

    private final String firstName;

    private final String lastName;

    private final String roleId;

    private final String roleName;

    @Setter
    private Map<String, Object> attributes;

    @JsonIgnore
    private final String username;

    @JsonIgnore
    private String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String id, String firstName, String lastName, String username, String password,
                         Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this(id, firstName, lastName, username, password, authorities, attributes, "", "USER");
    }

    public UserPrincipal(String username, Collection<? extends GrantedAuthority> authorities) {
        this(null, "", "", username, "", authorities, new HashMap<>(), "", "USER");
    }

    public UserPrincipal(String id, String firstName, String lastName, String username, String password,
                         Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes,
                         String roleId, String roleName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.attributes = attributes;
        this.authorities = authorities == null ? null : new ArrayList<>(authorities);
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        String roleId = user.getRole() != null ? String.valueOf(user.getRole().getId()) : "";

        authorities.add(new SimpleGrantedAuthority(roleName));

        return new UserPrincipal(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                new HashMap<>(),
                roleId,
                roleName
        );
    }

    public static OAuth2User create(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        String roleId = user.getRole() != null ? String.valueOf(user.getRole().getId()) : "";

        authorities.add(new SimpleGrantedAuthority(roleName));

        return new UserPrincipal(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                attributes,
                roleId,
                roleName
        );
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }


    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities == null ? null : new ArrayList<>(authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        UserPrincipal that = (UserPrincipal) object;
        return Objects.equals(id, that.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String getName() {
        return username;
    }
}
