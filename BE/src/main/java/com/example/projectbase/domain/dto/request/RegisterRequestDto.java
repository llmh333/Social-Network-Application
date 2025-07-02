package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.GenderConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String username;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Email(message = ErrorMessage.INVALID_EMAIL)
    private String email;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Pattern(regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#$%^&*(),.?\":{}|<>]{6,}$", message = ErrorMessage.INVALID_PASSWORD)
    private String password;

    @NotBlank String firstName;

    @NotBlank String lastName;

    @NotNull
    LocalDate dob;
    private GenderConstant gender;
}
