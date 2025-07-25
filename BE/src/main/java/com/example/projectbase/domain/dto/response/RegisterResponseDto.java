package com.example.projectbase.domain.dto.response;

import com.example.projectbase.constant.GenderConstant;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponseDto {

    private String username;
    private String email;
    private String lastName;
    private String firstName;
    private String gender;
}
