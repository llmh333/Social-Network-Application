package com.example.projectbase.domain.dto.response;

import com.example.projectbase.constant.MediaType;
import com.example.projectbase.domain.entity.PostCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private Long shareCount;
    private String createdBy;
    private MediaType mediaType;
    private PostCategory category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long originalPostId;
    
    private LocalDateTime createdAt;

    private boolean reactedByCurrentUser;
}



