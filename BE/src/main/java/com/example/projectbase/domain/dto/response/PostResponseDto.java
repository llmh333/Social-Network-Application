package com.example.projectbase.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long reactionCount;
    private Long commentCount;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private List<MediaResponseDto> mediaList;
}



