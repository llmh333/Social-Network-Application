package com.example.projectbase.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.tomcat.jni.Local;

import java.time.Instant;
import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private Local updatedAt;
    private List<MediaResponseDto> mediaList;
}



