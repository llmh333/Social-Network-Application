package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@RestApiV1
@Tag(name = "INFO TOKEN", description = "API lấy thông tin token sau khi đăng nhập bằng google hoặc facebook thành công")
public class OauthController {

    @Operation(description = "Trả về access token và refresh token của người dùng vừa đăng nhập")
    @GetMapping(UrlConstant.OAUTH2_INFO.OAUTH2_TOKEN_INFO)
    public ResponseEntity<?> getOauth2TokenInfo(@CookieValue(value = "accessToken", required = false) String accessToken,
                                                @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        if (accessToken == null || refreshToken == null) {
            return VsResponseUtil.error(HttpStatus.UNAUTHORIZED, "Token not found");
        }
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        return VsResponseUtil.success(response);
    }
}
