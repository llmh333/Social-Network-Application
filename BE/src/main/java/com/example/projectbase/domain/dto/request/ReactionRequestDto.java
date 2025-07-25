package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.ErrorMessage;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReactionRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String reactionType;

}
