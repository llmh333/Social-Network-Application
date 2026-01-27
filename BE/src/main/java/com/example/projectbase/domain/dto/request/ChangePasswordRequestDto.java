package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.ErrorMessage;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class ChangePasswordRequestDto {

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    private String oldPassword;

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    @Pattern(regexp = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};:\"\\\\|,.<>\\/?])(.{6,})$", message = ErrorMessage.INVALID_PASSWORD)
    private String newPassword;
}
