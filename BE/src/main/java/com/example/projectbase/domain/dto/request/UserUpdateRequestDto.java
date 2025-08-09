package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.GenderConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserUpdateRequestDto {

  private String firstName;

  private String lastName;

  private LocalDate dob;

  private GenderConstant gender;

  private String bio;

}
