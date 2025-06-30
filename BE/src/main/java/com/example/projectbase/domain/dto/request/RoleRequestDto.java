package com.example.projectbase.domain.dto.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class RoleRequestDto {

    @NotBlank(message = "Role name must not be blank")
    @Pattern(regexp = "^ROLE_(ADMIN|USER)$", message = "Role must be ROLE_ADMIN or ROLE_USER")
    private String name;

}
