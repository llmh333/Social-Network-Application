package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.ErrorMessage;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmNewPasswordRequestDto {

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    private String resetPasswordToken;

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = ErrorMessage.INVALID_PASSWORD)
    private String newPassword;

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    private String confirmNewPassword;
}
