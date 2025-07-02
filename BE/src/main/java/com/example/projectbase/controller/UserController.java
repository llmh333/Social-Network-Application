package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.request.ChangePasswordRequestDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateDto;
import com.example.projectbase.security.CurrentUser;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    return VsResponseUtil.success(userService.getCurrentUser(principal));
  }

  @PostMapping(UrlConstant.User.CREATE_USER)
  public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDto dto){
    return VsResponseUtil.success(userService.createUser(dto));
  }

  @GetMapping(UrlConstant.User.GET_ALL_USERS)
  public ResponseEntity<?> getAllUsers(@Valid @ParameterObject PaginationFullRequestDto requestDTO) {
      return VsResponseUtil.success(userService.getAllUsers(requestDTO));
  }

  @PutMapping(UrlConstant.User.UPDATE_USERNAME)
  public ResponseEntity<?> updateUser(@PathVariable String id,
                                      @Valid @RequestBody UserUpdateDto dto) {
    return VsResponseUtil.success(userService.updateUserName(id, dto));
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
