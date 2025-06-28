package com.example.projectbase.domain.dto.response;


import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SharePostResponseDto {
    private Long id;
    private String title;
    private String content;
    private String createdBy;
    private Long shareCount;
    private PostSummaryDto originalPost;
}
