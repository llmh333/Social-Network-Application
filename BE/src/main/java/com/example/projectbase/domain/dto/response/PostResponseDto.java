package com.example.projectbase.domain.dto.response;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private List<MediaResponseDto> mediaList;
    private Long reactionCount;
    private Long commentCount;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
}



