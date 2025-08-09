package com.example.projectbase.domain.dto.response;

import com.example.projectbase.constant.GenderConstant;
import com.example.projectbase.domain.dto.common.DateAuditingDto;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UserResponseDto {

  private String id;

  private String username;

  private String firstName;

  private String lastName;

  private String email;

  private String imageUrl;

  private LocalDate dob;

  private String bio;

  private GenderConstant gender;

  private long totalFollowers;

  private long totalFollowings;

  private LocalDateTime createdAt;

  private LocalDateTime lastModifiedAt;

}

