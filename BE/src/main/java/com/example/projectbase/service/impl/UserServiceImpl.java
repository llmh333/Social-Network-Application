package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.SortByDataConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.UserCreateDto;
import com.example.projectbase.domain.dto.request.UserUpdateDto;
import com.example.projectbase.domain.dto.response.UserDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final RoleRepository roleRepository;

  private final UserMapper userMapper;

  @Override
  public UserDto getUserById(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
    return userMapper.toUserDto(user);
  }

  @Override
  public PaginationResponseDto<UserDto> getCustomers(PaginationFullRequestDto request) {
    //Pagination
    Pageable pageable = PaginationUtil.buildPageable(request, SortByDataConstant.USER);
    //Create Output
    return new PaginationResponseDto<>(null, null);
  }

  @Override
  public UserDto getCurrentUser(UserPrincipal principal) {
    User user = userRepository.getUser(principal);
    return userMapper.toUserDto(user);
  }

  @Override
  public UserDto createUser(UserCreateDto dto) {
    User user = userMapper.toUser(dto);
    Role role = roleRepository.findByRoleName("USER")
            .orElseThrow(() -> new RuntimeException("Default role not found"));
    user.setRole(role);
    return userMapper.toUserDto(userRepository.save(user));
  }

  @Override
  public List<UserDto> getAllUsers() {
    List<User> users = userRepository.findAll();
    return users.stream()
            .map(userMapper::toUserDto)
            .collect(Collectors.toList());
  }

  @Override
  public UserDto updateUserName(String id, UserUpdateDto dto) {
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



//  private LoginResponseDto validateEmailAndPassword(String email, String password) {
//    // Email regex pattern
//    String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
//    Pattern emailPattern = Pattern.compile(emailRegex);
//
//    // Password regex pattern
//    // The following regex ensures that the password is at least 8 characters long,
//    // contains at least one digit, one lower case letter, one upper case letter,
//    // and one special character.
//    String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
//    Pattern passwordPattern = Pattern.compile(passwordRegex);
//
//    if (!emailPattern.matcher(email).matches()) {
//      return new LoginResponseDto("Invalid email format", false);
//    }
//
//    if (!passwordPattern.matcher(password).matches()) {
//      return new LoginResponseDto("Password must be at least 8 characters long, "
//              + "contain at least one digit, one lower case letter, "
//              + "one upper case letter, and one special character", false);
//    }
//
//    return new LoginResponseDto("Validation successful", true);
//  }




}
