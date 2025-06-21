package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateDto;
import com.example.projectbase.domain.dto.response.LoginResponseDto;
import com.example.projectbase.domain.dto.response.UserDto;
import com.example.projectbase.security.CurrentUser;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestApiV1
public class UserController {

  private final UserService userService;

  @Tag(name = "user-controller-admin")
  @Operation(summary = "API get user")
  @GetMapping(UrlConstant.User.GET_USER)
  public ResponseEntity<?> getUserById(@PathVariable String userId) {
    return VsResponseUtil.success(userService.getUserById(userId));
  }

  @Tags({@Tag(name = "user-controller-admin"), @Tag(name = "user-controller")})
  @Operation(summary = "API get current user login")
  @GetMapping(UrlConstant.User.GET_CURRENT_USER)
  public ResponseEntity<?> getCurrentUser(@Parameter(name = "principal", hidden = true)
                                          @CurrentUser UserPrincipal principal) {
    return VsResponseUtil.success(userService.getCurrentUser(principal));
  }

  @Tag(name = "user-controller-admin")
  @Operation(summary = "API get all customer")
  @GetMapping(UrlConstant.User.GET_USERS)
  public ResponseEntity<?> getCustomers(@Valid @ParameterObject PaginationFullRequestDto requestDTO) {
    return VsResponseUtil.success(userService.getCustomers(requestDTO));
  }

  @Tag(name ="user-controller-admin")
  @Operation(summary =" API create user")
  @PostMapping(UrlConstant.User.CREATE_USER)
  public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDto dto){
    return VsResponseUtil.success(userService.createUser(dto));
  }

  @Tag(name ="user-controller-admin")
  @Operation(summary =" API get all users")
  @GetMapping(UrlConstant.User.GET_ALL_USERS)
  public ResponseEntity<?> getAllUsers(@Valid @ParameterObject PaginationFullRequestDto requestDTO) {
      return VsResponseUtil.success(userService.getAllUsers(requestDTO));
  }

  @Tag(name = "user-controller-admin")
  @Operation(summary = "API update user")
  @PutMapping(UrlConstant.User.UPDATE_USERNAME)
  public ResponseEntity<?> updateUser(@PathVariable String id,
                                      @Valid @RequestBody UserUpdateDto dto) {
    return VsResponseUtil.success(userService.updateUserName(id, dto));
  }

  @Tag(name = "user-controller-admin")
  @Operation(summary = "API delete user")
  @DeleteMapping(UrlConstant.User.DELETE_USER)
  public ResponseEntity<?> deleteUser(@PathVariable String id){
    userService.deleteUser(id);
    return VsResponseUtil.success("User deleted successfully");
  }

}
