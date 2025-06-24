package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.request.LoginRequestDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.response.CommonResponseDto;
import com.example.projectbase.domain.dto.response.LoginResponseDto;
import com.example.projectbase.domain.dto.response.SignUpResponseDto;
import com.example.projectbase.domain.dto.request.RegisterRequestDto;
import com.example.projectbase.domain.dto.request.TokenRefreshRequestDto;
import com.example.projectbase.service.AuthService;
import com.example.projectbase.validator.annotation.ValidFileImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@Validated
@RestApiV1
@Tag(name = "Login", description = "Các API đăng nhập với Tài khoản mật khẩu và Google, Facebook")
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "API Đăng ký tài khoản")
  @PostMapping(UrlConstant.Auth.REGISTER)
  public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto req) {
    LoginResponseDto tokens = authService.register(req);
    return VsResponseUtil.success(HttpStatus.CREATED, tokens);
  }

  @Operation(
          summary = "Đăng nhập bằng username & password",
          description = "Truyền vào username và password hợp lệ để nhận access token & refresh token"
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
          @ApiResponse(responseCode = "401", description = "Sai thông tin đăng nhập")
  })
  @PostMapping(UrlConstant.Auth.LOGIN)
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
    return VsResponseUtil.success(authService.login(request));
  }

  @Operation(summary = "API test")
  @PostMapping("auth/test")
  public String login(@ValidFileImage MultipartFile multipartFile) {
    return multipartFile.getContentType();
  }


  @Operation(summary = "API signup")
  @PostMapping("/signup")
  public ResponseEntity<SignUpResponseDto> signUp(@Valid @RequestBody UserCreateDto request) {
    SignUpResponseDto response = authService.signUp(request);
    return ResponseEntity.ok(response);
  }

  @Operation(
          summary = "API Logout",
          description = "Xóa cookie chứa accessToken và refreshToken nếu có"
  )
  @PostMapping(UrlConstant.Auth.LOGOUT)
  public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {

    Cookie clearAccessToken = new Cookie("accessToken", "");
    clearAccessToken.setMaxAge(0);
    clearAccessToken.setPath("/");
    clearAccessToken.setHttpOnly(true);
    clearAccessToken.setSecure(true);
    response.addCookie(clearAccessToken);

    Cookie clearRefreshToken = new Cookie("refreshToken", "");
    clearRefreshToken.setMaxAge(0);
    clearRefreshToken.setPath("/");
    clearRefreshToken.setHttpOnly(true);
    clearRefreshToken.setSecure(true);
    response.addCookie(clearRefreshToken);
    return VsResponseUtil.success(authService.logout(request));
  }

  @Operation(
          summary = "API Lấy thông tin người dùng sau khi đăng nhập Google OAuth2"
  )
  @GetMapping(UrlConstant.Auth.OAUTH2_INFO)
  public ResponseEntity<?> getOAuth2Info(@AuthenticationPrincipal OAuth2User principal) {
    if (principal == null) {
      return VsResponseUtil.error(HttpStatus.UNAUTHORIZED,"Chưa đăng nhập");
    }
    Map<String, Object> response = new HashMap<>();
    response.put("name", principal.getAttribute("name"));
    response.put("email", principal.getAttribute("email"));
    response.put("picture", principal.getAttribute("picture"));
    return VsResponseUtil.success(response);
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
      return VsResponseUtil.error(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
    return VsResponseUtil.success(principal);
  }

  @Operation(
          summary = "API Lấy redirect login google",
          description = "Lấy redirect url chuyển sang trang đăng nhập google"
  )
  @GetMapping(UrlConstant.Auth.LOGIN_GOOGLE)
  public ResponseEntity<?> getRedirectToGoogle() throws IOException {
    return VsResponseUtil.success(HttpStatus.FOUND, UrlConstant.OAUTH2_INFO.REDIRECT_OAUTH2_GOOGLE);
  }

  @Operation(
          summary = "API Lấy redirect login facebook",
          description = "Lấy redirect url chuyển sang trang đăng nhập facebook"
  )
  @GetMapping(UrlConstant.Auth.LOGIN_FACEBOOK)
  public ResponseEntity<?> getRedirectToFacebook() throws IOException {
    return VsResponseUtil.success(HttpStatus.FOUND, UrlConstant.OAUTH2_INFO.REDIRECT_OAUTH2_FACEBOOK);
  }
}
