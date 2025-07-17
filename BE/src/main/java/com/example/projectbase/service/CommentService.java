package com.example.projectbase.service;

import com.example.projectbase.domain.dto.request.CommentRequestDto;
import com.example.projectbase.domain.dto.request.ReplyCommentRequestDto;
import com.example.projectbase.domain.dto.response.CommentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    CommentResponseDto addComment(Long postId, CommentRequestDto requestDto, String username);
    CommentResponseDto replyToComment(Long postId, ReplyCommentRequestDto requestDto, String username);
    CommentResponseDto updateComment(Long commentId, String content, Long postId, String username);
    void deleteComment(Long postId, Long commentId, String username);
    Page<CommentResponseDto> getCommentsByPost(Long postId, Pageable pageable);
    List<CommentResponseDto> getRepliesByParentId(Long postId, Long parentId);
    CommentResponseDto getCommentWithReplies(Long postId, Long commentId);

}