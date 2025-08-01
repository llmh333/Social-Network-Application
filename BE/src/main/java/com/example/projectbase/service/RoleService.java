package com.example.projectbase.service;

import com.example.projectbase.domain.dto.request.RoleRequestDto;
import com.example.projectbase.domain.dto.response.RoleResponseDto;
import com.example.projectbase.security.UserPrincipal;

import java.util.List;

public interface RoleService {
    RoleResponseDto createRole(RoleRequestDto request);
    RoleResponseDto getRoleById(Long id, UserPrincipal currentUser);
    List<RoleResponseDto> getAllRoles();
    RoleResponseDto updateRole(Long id, RoleRequestDto request);
    void deleteRole(Long id);
}
