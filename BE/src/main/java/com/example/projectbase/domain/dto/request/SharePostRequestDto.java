package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SharePostRequestDto {

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    private String title;

    @NotNull(message = ErrorMessage.NOT_BLANK_FIELD)
    private String content;
}
