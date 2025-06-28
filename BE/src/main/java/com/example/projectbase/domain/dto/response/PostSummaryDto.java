package com.example.projectbase.domain.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSummaryDto {
    private Long id;
    private String  title;
    private String content;
    private String createdBy;
    private Long shareCount;
}