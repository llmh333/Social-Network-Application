package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.request.ChangePasswordRequestDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateRequestDto;
import com.example.projectbase.domain.dto.response.UserResponseDto;
import com.example.projectbase.security.CurrentUser;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.security.Principal;

@RequiredArgsConstructor
@RestApiV1
@Tag(name = "User Controller", description = "Các chức năng liên quan tới user")
public class UserController {

  private final UserService userService;

  @GetMapping(UrlConstant.User.GET_USER)
  public ResponseEntity<?> getUserById(@PathVariable String userId) {
    return VsResponseUtil.success(userService.getUserById(userId));
  }

  @GetMapping(UrlConstant.User.GET_CURRENT_USER)
  public ResponseEntity<?> getCurrentUser(@Parameter(name = "principal", hidden = true)
                                          @CurrentUser UserPrincipal principal) {
    UserResponseDto userResponseDto = userService.getCurrentUser(principal);
    return VsResponseUtil.success(userResponseDto);
  }

  @PostMapping(UrlConstant.User.CREATE_USER)
  public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDto dto){
    return VsResponseUtil.success(userService.createUser(dto));
  }

  @GetMapping(UrlConstant.User.GET_USERS)
  public ResponseEntity<?> getAllUsers(@Valid @ParameterObject PaginationFullRequestDto requestDTO) {
      return VsResponseUtil.success(userService.getAllUsers(requestDTO));
  }

  @PutMapping(UrlConstant.User.UPDATE)
  public ResponseEntity<?> updateUser(@PathVariable String id,
                                      @Valid @RequestBody UserUpdateRequestDto request) {
    return VsResponseUtil.success(userService.updateUserInformation(id, request));
  }

  @PutMapping(UrlConstant.User.UPDATE_AVATAR)
  public ResponseEntity<?> updateUserAvatar(@RequestParam("file") MultipartFile file) throws IOException {
    File tempFile = File.createTempFile("_upload_", file.getOriginalFilename());
    file.transferTo(tempFile);
    UserResponseDto response = userService.updateUserAvatar(tempFile, file.getContentType());
    return VsResponseUtil.success(response);
  }

  @DeleteMapping(UrlConstant.User.DELETE_USER)
  public ResponseEntity<?> deleteUser(@PathVariable String id){
    userService.deleteUser(id);
    return VsResponseUtil.success("User deleted successfully");
  }

  @PutMapping(UrlConstant.User.CHANGE_PASSWORD)
  public ResponseEntity<?> changePasswordUser(@RequestBody ChangePasswordRequestDto request,
                                          Principal principal){
    String username = principal.getName();
    userService.changePassword(username, request);
    return VsResponseUtil.success("Password changed successfully");



  }


}
