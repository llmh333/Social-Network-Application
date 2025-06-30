package com.example.projectbase.service.impl;

import com.example.projectbase.constant.RoleConstant;
import com.example.projectbase.domain.dto.request.RoleRequestDto;
import com.example.projectbase.domain.dto.response.RoleResponseDto;
import com.example.projectbase.domain.entity.Role;
import com.example.projectbase.domain.mapper.RoleMapper;
import com.example.projectbase.exception.ForbiddenException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.exception.RoleAlreadyExistsException;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private UserRepository userRepository;

    @Override
    public RoleResponseDto createRole(RoleRequestDto request) {
        validateRoleName(request.getName());
        validateDuplicateRole(request.getName());

        Role role = roleMapper.toEntity(request);
        return roleMapper.toRoleResponseDto(roleRepository.save(role));
    }

    public RoleResponseDto getRoleById(Long id, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRoleName().equalsIgnoreCase("ROLE_ADMIN");

        if (!isAdmin && !Objects.equals(currentUser.getRoleId(), String.valueOf(id))) {
            throw new ForbiddenException("Bạn chỉ được xem role của chính mình.");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role không tồn tại"));

        return roleMapper.toRoleResponseDto(role);
    }

    @Override
    public List<RoleResponseDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponseDto updateRole(Long id, RoleRequestDto request) {
        validateRoleName(request.getName());

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!role.getName().equals(request.getName())) {
            validateDuplicateRole(request.getName());
        }

        role.setName(request.getName());
        return roleMapper.toRoleResponseDto(roleRepository.save(role));
    }

    @Override
    public void deleteRole(Long id) {
        if (userRepository.existsByRole_Id(id)) {
            throw new IllegalStateException("Cannot delete role assigned to existing users.");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }

    private void validateRoleName(String name) {
        if (!RoleConstant.ADMIN.equals(name) && !RoleConstant.USER.equals(name)) {
            throw new IllegalArgumentException("Only ROLE_ADMIN or ROLE_USER are allowed");
        }
        if (roleRepository.existsByName(name)) {
            throw new RoleAlreadyExistsException(name);
        }
    }

    private void validateDuplicateRole(String roleName) {
        if (roleRepository.existsByName(roleName)) {
            throw new IllegalArgumentException("Role name already exists: " + roleName);
        }
    }
}
