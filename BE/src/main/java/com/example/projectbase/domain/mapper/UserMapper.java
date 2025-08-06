package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateRequestDto;
import com.example.projectbase.domain.dto.response.RegisterResponseDto;
import com.example.projectbase.domain.dto.response.UserResponseDto;
import com.example.projectbase.domain.dto.response.UserSummaryDto;
import com.example.projectbase.domain.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  User toUser(UserCreateDto userCreateDTO);

  default UserResponseDto toUserDto(User user) {
    long totalFollowers = user.getFollowers().size();
    long totalFollowings = user.getFollowings().size();
    UserResponseDto userResponseDto = new UserResponseDto();
    userResponseDto.setId(user.getId());
    userResponseDto.setUsername(user.getUsername());
    userResponseDto.setFirstName(user.getFirstName());
    userResponseDto.setLastName(user.getLastName());
    userResponseDto.setEmail(user.getEmail());
    userResponseDto.setImageUrl(user.getImageUrl());
    userResponseDto.setDob(user.getDob());
    userResponseDto.setGender(user.getGender());
    userResponseDto.setCreatedAt(user.getCreatedAt());
    userResponseDto.setLastModifiedAt(user.getLastModifiedAt());
    userResponseDto.setTotalFollowers(totalFollowers);
    userResponseDto.setTotalFollowings(totalFollowings);

    return userResponseDto;
  }

  List<UserResponseDto> toUserDtos(List<User> user);

  void updateUserFromDto(UserUpdateRequestDto dto, @MappingTarget User user);

  UserSummaryDto toUserSummaryDto(User user);

  RegisterResponseDto toRegisterDto(User user);
}
