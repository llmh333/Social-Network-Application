// BE/src/main/java/com/example/projectbase/domain/dto/request/RegisterRequestDto.java
package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.GenderConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDto {
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Password không được để trống")
    private String password;

    @NotBlank String firstName;

    @NotBlank String lastName;

    @NotNull
    LocalDate dob;
    private GenderConstant gender;
}
