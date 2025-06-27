package com.example.projectbase.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SharePostRequestDto {
    private Long originalPostId;
    private String title;
    private String content;
}
