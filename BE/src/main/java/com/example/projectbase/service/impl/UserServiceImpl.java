package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.SortByDataConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PagingMeta;
import com.example.projectbase.domain.dto.request.ChangePasswordRequestDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateDto;
import com.example.projectbase.domain.dto.response.UserResponseDto;
import com.example.projectbase.domain.entity.Role;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.UserMapper;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.UserService;
import com.example.projectbase.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final RoleRepository roleRepository;

  private final UserMapper userMapper;

  private final PasswordEncoder passwordEncoder;

  @Override
  public UserResponseDto getUserById(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
    return userMapper.toUserDto(user);
  }

  @Override
  public UserResponseDto getCurrentUser(UserPrincipal principal) {
    User user = userRepository.getUser(principal);
    return userMapper.toUserDto(user);
  }

  @Override
  public UserResponseDto createUser(UserCreateDto dto) {
    User user = userMapper.toUser(dto);
    Role role = roleRepository.findByRoleName("USER")
            .orElseThrow(() -> new RuntimeException("Default role not found"));
    user.setRole(role);
    return userMapper.toUserDto(userRepository.save(user));
  }

  @Override
  public PaginationResponseDto<UserResponseDto> getAllUsers(PaginationFullRequestDto request) {
    Pageable pageable = PaginationUtil.buildPageable(request, SortByDataConstant.USER);

    Page<User> pageUser = userRepository.findAll(pageable);

    List<UserResponseDto> userResponseDtos = pageUser.getContent().stream()
            .map(userMapper::toUserDto)
            .collect(Collectors.toList());

    String sortBy = "";
    String sortType = "";

    if (pageUser.getSort().isSorted()) {
      Sort.Order order = pageUser.getSort().iterator().next();
      sortBy = order.getProperty();
      sortType = order.getDirection().name().toLowerCase();
    } else {
      sortBy = "id";
      sortType = "asc";
    }

    PagingMeta meta = new PagingMeta(
            pageUser.getTotalElements(),
            pageUser.getTotalPages(),
            pageUser.getNumber(),
            pageUser.getSize(),
            sortBy,
            sortType
    );

    return new PaginationResponseDto<>(meta, userResponseDtos);

  }

  @Override
  public UserResponseDto updateUserName(String id, UserUpdateDto dto) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

    userMapper.updateUserFromDto(dto, user);

    user = userRepository.save(user);
    return userMapper.toUserDto(user);
  }

  @Override
  public void deleteUser(String id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    userRepository.delete(user);
  }

  @Override
  public void changePassword(String username, ChangePasswordRequestDto request) {
      User user = userRepository.findByUsername(username)
              .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Old password is incorrect");
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
}


}
