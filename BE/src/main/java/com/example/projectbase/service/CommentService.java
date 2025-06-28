package com.example.projectbase.service;

import com.example.projectbase.domain.dto.request.CommentRequestDto;
import com.example.projectbase.domain.dto.request.ReplyRequestDto;
import com.example.projectbase.domain.dto.response.CommentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    CommentResponseDto addComment(Long postId, CommentRequestDto dto, String username);
    CommentResponseDto replyToComment(Long postId, Long parentId, ReplyRequestDto dto, String username);
    CommentResponseDto updateComment(Long postId, Long commentId, CommentRequestDto dto, String username);
    void deleteComment(Long postId, Long commentId, String username);
    Page<CommentResponseDto> getCommentsByPost(Long postId, Pageable pageable);
    List<CommentResponseDto> getRepliesByParentId(Long parentId);
    Optional<CommentResponseDto> getCommentWithReplies(Long commentId);

}