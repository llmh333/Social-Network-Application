package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateDto;
import com.example.projectbase.domain.dto.response.UserResponseDto;
import com.example.projectbase.domain.dto.response.UserSummaryDto;
import com.example.projectbase.domain.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

  User toUser(UserCreateDto userCreateDTO);

  @Mappings({
      @Mapping(target = "roleName", source = "user.role.name"),
  })
  UserResponseDto toUserDto(User user);

  List<UserResponseDto> toUserDtos(List<User> user);

  void updateUserFromDto(UserUpdateDto dto, @MappingTarget User user);

  UserSummaryDto toUserSummaryDto(User user);
}
