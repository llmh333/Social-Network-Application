package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequestDto {

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String usernameOrEmail;

  @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
  private String password;


}
