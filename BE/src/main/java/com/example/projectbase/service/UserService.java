package com.example.projectbase.service;

import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.ChangePasswordRequestDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateRequestDto;
import com.example.projectbase.domain.dto.response.UserResponseDto;
import com.example.projectbase.security.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface UserService {

  UserResponseDto getUserById(String userId);

  UserResponseDto getCurrentUser(UserPrincipal principal);

  UserResponseDto createUser(UserCreateDto dto);

  PaginationResponseDto<UserResponseDto> getAllUsers(PaginationFullRequestDto request);

  UserResponseDto updateUserInformation(String id, UserUpdateRequestDto request);

  UserResponseDto updateUserAvatar(File avatarFile, String contentType);

  void deleteUser(String id);
  
  void changePassword(String username, ChangePasswordRequestDto dto);


}
