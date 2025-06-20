package com.example.projectbase.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDto {

    @NotBlank(message = "content must not be blank")
    private String content;
    private List<String> mediaIds;
}
