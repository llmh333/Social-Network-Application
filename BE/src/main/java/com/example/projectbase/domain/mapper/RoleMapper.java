package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.request.RoleRequestDto;
import com.example.projectbase.domain.dto.response.RoleResponseDto;
import com.example.projectbase.domain.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleRequestDto dto);

    RoleResponseDto toRoleResponseDto(Role role);

}
