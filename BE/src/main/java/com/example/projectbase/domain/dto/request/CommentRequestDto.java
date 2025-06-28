package com.example.projectbase.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {

    @NotBlank(message = "Content must not be blank")
    private String content;

    @NotNull(message = "Post ID must not be null")
    private Long postId;

}
