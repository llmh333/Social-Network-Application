package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.ErrorMessage;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    String followingId;


}
