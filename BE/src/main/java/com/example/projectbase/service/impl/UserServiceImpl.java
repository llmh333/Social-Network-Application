package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.RoleConstant;
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
import com.example.projectbase.domain.entity.UserSession;
import com.example.projectbase.domain.mapper.UserMapper;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.RoleRepository;
import com.example.projectbase.repository.TokenBlacklistRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.repository.UserSessionRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.UserService;
import com.example.projectbase.util.PaginationUtil;
import com.example.projectbase.util.TokenBlacklistUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final RoleRepository roleRepository;

  private final UserSessionRepository userSessionRepository;

  private final TokenBlacklistRepository tokenBlacklistRepository;

  private final UserMapper userMapper;

  private final PasswordEncoder passwordEncoder;

  @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
  @Override
  public UserResponseDto getUserById(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
    return userMapper.toUserDto(user);
  }

  @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
  @Override
  public UserResponseDto getCurrentUser(UserPrincipal principal) {
    User user = userRepository.getUser(principal);
    return userMapper.toUserDto(user);
  }

  @Override
  public UserResponseDto createUser(UserCreateDto dto) {
    User user = userMapper.toUser(dto);
    Role role = roleRepository.findByRoleName("USER")
            .orElseThrow(() -> new NotFoundException(ErrorMessage.Role.ERR_NOT_FOUND, new String[]{RoleConstant.USER.toString()}));
    user.setRole(role);
    return userMapper.toUserDto(userRepository.save(user));
  }

  @PreAuthorize("hasRole('ADMIN')")
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

  @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
  @Override
  public UserResponseDto updateUserName(String id, UserUpdateDto dto) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id}));

    userMapper.updateUserFromDto(dto, user);

    user = userRepository.save(user);
    return userMapper.toUserDto(user);
  }

  @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
  @Override
  @Transactional
  public void deleteUser(String id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id}));
    List<UserSession> userSessions = userSessionRepository.findAllByUsername(user.getUsername());
    if (!userSessions.isEmpty()) {
      userSessions.forEach(userSession -> {
        TokenBlacklistUtil.addTokenToBlacklist(userSession.getToken(), "Logout token", tokenBlacklistRepository);
        TokenBlacklistUtil.addTokenToBlacklist(userSession.getRefreshToken(), "Logout refresh token", tokenBlacklistRepository);
      });
    }
    userSessionRepository.deleteAllByUsername(user.getUsername());
    userRepository.delete(user);
  }

  @PreAuthorize("#username == authentication.principal.username or hasRole('ADMIN')")
  @Override
  public void changePassword(String username, ChangePasswordRequestDto request) {
      User user = userRepository.findByUsername(username)
              .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{username}));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_OLD_PASSWORD_INCORRECT);
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }


}
