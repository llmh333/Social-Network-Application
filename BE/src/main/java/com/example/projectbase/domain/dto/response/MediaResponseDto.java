package com.example.projectbase.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MediaResponseDto {

    private String publicId;
    private String secureUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String playbackUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thumbnailUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String postId;

    private String resourceType;
    private Long dataSize;
    private String format;
    private String authorId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long height;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long width;

    private LocalDateTime createdAt;
}
