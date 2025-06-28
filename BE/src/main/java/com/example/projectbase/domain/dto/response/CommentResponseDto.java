package com.example.projectbase.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private Long postId;
    private Long parentCommentId;
    private UserSummaryDto author;
    private List<CommentResponseDto> replies;
    private Long replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
