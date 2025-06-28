package com.example.projectbase.service.impl;

import com.example.projectbase.domain.dto.request.CommentRequestDto;
import com.example.projectbase.domain.dto.request.ReplyRequestDto;
import com.example.projectbase.domain.dto.response.CommentResponseDto;
import com.example.projectbase.domain.entity.Comment;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.CommentMapper;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.exception.UnauthorizedException;
import com.example.projectbase.repository.CommentRepository;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentResponseDto addComment(Long postId, CommentRequestDto dto, String username) {
        log.debug("Adding comment to post {} by user {}", postId, username);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        Comment comment = createComment(dto.getContent(), post, user, null);

        Comment savedComment = commentRepository.save(comment);
        log.debug("Comment created with id: {}", savedComment.getId());

        return commentMapper.toCommentResponseDto(savedComment);
    }

    @Override
    @Transactional
    public CommentResponseDto replyToComment(Long postId, Long parentId, ReplyRequestDto dto, String username) {
        log.debug("Adding reply to comment {} on post {} by user {}", parentId, postId, username);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found with id: " + parentId));

        if (!parent.getPost().getId().equals(postId)) {
            throw new NotFoundException("Parent comment does not belong to this post");
        }

        if (parent.getCommentLevel() >= 1) {
            throw new IllegalArgumentException("Chỉ cho phép trả lời bình luận gốc (tối đa 2 cấp).");
        }

        Comment reply = createComment(dto.getContent(), post, user, parent);

        Comment savedReply = commentRepository.save(reply);
        log.debug("Reply created with id: {}", savedReply.getId());

        return commentMapper.toCommentResponseDto(savedReply);
    }


    @Override
    @Transactional
    public CommentResponseDto updateComment(Long postId, Long commentId, CommentRequestDto dto, String username) {
        log.debug("Updating comment {} on post {} by user {}", commentId, postId, username);

        Comment comment = findCommentWithUserAndPost(commentId);
        validateCommentOwnership(comment, postId, username);

        comment.setContent(dto.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.debug("Comment {} updated successfully", commentId);
        return commentMapper.toCommentResponseDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, String username) {
        log.debug("Deleting comment {} on post {} by user {}", commentId, postId, username);

        Comment comment = findCommentWithUserAndPost(commentId);
        validateCommentOwnership(comment, postId, username);

        long replyCount = commentRepository.countByParentId(commentId);
        if (replyCount > 0) {
            comment.setContent("[Bình luận đã bị xóa]");
            commentRepository.save(comment);
            log.debug("Comment {} soft deleted (has {} replies)", commentId, replyCount);
        } else {
            commentRepository.delete(comment);
            log.debug("Comment {} hard deleted", commentId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByPost(Long postId, Pageable pageable) {
        log.debug("Getting comments for post {} with lazy loading", postId);
        return commentRepository.findParentCommentsByPostId(postId, pageable)
                .map(comment -> {
                    CommentResponseDto dto = commentMapper.toCommentResponseDto(comment);
                    dto.setReplyCount(commentRepository.countByParentId(comment.getId()));
                    return dto;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getRepliesByParentId(Long parentId) {
        log.debug("Getting replies for parent comment {}", parentId);
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId)
                .stream()
                .map(commentMapper::toCommentResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommentResponseDto> getCommentWithReplies(Long commentId) {
        return commentRepository.findByIdAndParentIsNull(commentId)
                .map(comment -> {
                    CommentResponseDto dto = commentMapper.toCommentResponseDto(comment);
                    List<CommentResponseDto> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(comment.getId())
                            .stream()
                            .map(commentMapper::toCommentResponseDto)
                            .toList();
                    dto.setReplies(replies);
                    return dto;
                });
    }

    private Comment createComment(String content, Post post, User user, Comment parent) {
        int level = (parent == null) ? 0 : parent.getCommentLevel() + 1;
        try {
            return Comment.builder()
                    .content(content)
                    .post(post)
                    .user(user)
                    .parent(parent)
                    .commentLevel(level)
                    .build();
        } catch (Exception e) {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setPost(post);
            comment.setUser(user);
            comment.setParent(parent);
            comment.setCommentLevel(level);
            return comment;
        }
    }

    private Comment findCommentWithUserAndPost(Long commentId) {
        Optional<Comment> comment = commentRepository.findByIdWithUserAndPost(commentId);
        if (comment.isEmpty()) {
            comment = commentRepository.findById(commentId);
        }
        return comment.orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
    }

    private void validateCommentOwnership(Comment comment, Long postId, String username) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("Comment does not belong to this post");
        }
        if (!comment.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("You are not authorized to modify this comment");
        }
    }
}