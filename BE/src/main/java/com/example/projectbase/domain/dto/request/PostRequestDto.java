package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String title;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String content;

    private String singerName;

    private String category;

    private MediaType mediaType;

}
