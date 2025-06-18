package com.example.projectbase.service.impl;

import com.example.projectbase.domain.entity.User;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthServiceImpl {

    private final UserRepository userRepository;

    /**
     * Dùng sau khi Google auth thành công, lấy attributes và token để:
     * 1) tìm user hiện tại theo email/username
     * 2) nếu chưa có: tạo mới và lưu vào DB
     * 3) trả về UserPrincipal
     */
    public UserPrincipal processOAuthPostLogin(OAuth2AuthenticationToken authToken) {
        Map<String,Object> attrs = authToken.getPrincipal().getAttributes();

        System.out.println(attrs);
        // Giả sử Google trả về email trong attribute "email"
        String email = (String) attrs.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        User user;
        if (userOpt.isEmpty()) {

            user = new User();
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName((String) attrs.get("given_name"));
            user.setLastName((String) attrs.get("family_name"));
            user.setDob(LocalDate.of(1970,1,1));
            user = userRepository.save(user);
        } else {
            user = userOpt.get();
        }


        UserPrincipal principal = UserPrincipal.create(user);
        principal.setAttributes(attrs);

        return principal;
    }
}
