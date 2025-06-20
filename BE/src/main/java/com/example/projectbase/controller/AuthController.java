package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.request.LoginRequestDto;
import com.example.projectbase.domain.dto.request.RegisterRequestDto;
import com.example.projectbase.domain.dto.request.TokenRefreshRequestDto;
import com.example.projectbase.domain.dto.response.LoginResponseDto;
import com.example.projectbase.service.AuthService;
import com.example.projectbase.validator.annotation.ValidFileImage;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Validated
@RestApiV1
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "API Đăng ký tài khoản")
  @PostMapping(UrlConstant.Auth.REGISTER)
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto req) {
    LoginResponseDto tokens = authService.register(req);
    return VsResponseUtil.success(HttpStatus.CREATED, tokens);
  }

  @Operation(summary = "API Login")
  @PostMapping(UrlConstant.Auth.LOGIN)
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
    return VsResponseUtil.success(authService.login(request));
  }

  @GetMapping(UrlConstant.Auth.OAUTH2_LOGIN + "/google")
  public void googleLoginRedirect(HttpServletResponse res) throws IOException {
    res.sendRedirect("/oauth2/authorization/google");
  }

  @Operation(summary = "API test")
  @PostMapping("auth/test")
  public String login(@ValidFileImage MultipartFile multipartFile) {
    return multipartFile.getContentType();
  }

  @Operation(summary = "API Logout")
  @PostMapping(UrlConstant.Auth.LOGOUT)
  public ResponseEntity<?> logout(javax.servlet.http.HttpServletRequest request) {
    return VsResponseUtil.success(authService.logout(request));
  }

  @Operation(summary = "API Lấy thông tin người dùng sau khi đăng nhập Google OAuth2")
  @GetMapping(UrlConstant.Auth.OAUTH2_INFO)
  public ResponseEntity<?> getOAuth2Info(@AuthenticationPrincipal OAuth2User principal) {
    if (principal == null) {
      return ResponseEntity.status(401).body("Chưa đăng nhập");
    }
    Map<String, Object> response = new HashMap<>();
    response.put("name", principal.getAttribute("name"));
    response.put("email", principal.getAttribute("email"));
    response.put("picture", principal.getAttribute("picture"));
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "API Refresh Token")
  @PostMapping(UrlConstant.Auth.REFRESH_TOKEN)
  public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequestDto request) {
    return VsResponseUtil.success(authService.refresh(request));
  }

  @Operation(summary = "API Lấy thông tin người dùng hiện tại (local hoặc OAuth2)")
  @GetMapping(UrlConstant.Auth.ME)
  public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Object principal) {
    if (principal == null) {
      return ResponseEntity.status(401).body("Chưa đăng nhập");
    }
    return ResponseEntity.ok(principal);
  }
}
