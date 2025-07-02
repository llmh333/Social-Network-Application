package com.example.projectbase.service;

import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.ChangePasswordRequestDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateDto;
import com.example.projectbase.domain.dto.response.UserResponseDto;
import com.example.projectbase.security.UserPrincipal;

public interface UserService {

  UserResponseDto getUserById(String userId);

  UserResponseDto getCurrentUser(UserPrincipal principal);

  UserResponseDto createUser(UserCreateDto dto);

  PaginationResponseDto<UserResponseDto> getAllUsers(PaginationFullRequestDto request);

  UserResponseDto updateUserName(String id, UserUpdateDto dto);

  void deleteUser(String id);
  
  void changePassword(String username, ChangePasswordRequestDto dto);


}
