package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.dto.request.CommentRequestDto;
import com.example.projectbase.domain.dto.request.ReplyCommentRequestDto;
import com.example.projectbase.domain.dto.response.CommentResponseDto;
import com.example.projectbase.domain.entity.Comment;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.CommentMapper;
import com.example.projectbase.domain.mapper.UserMapper;
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

import java.util.ArrayList;
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
    private final UserMapper userMapper;

    @Override
    @Transactional
    public CommentResponseDto addComment(CommentRequestDto requestDto, String username) {

        Long postId = requestDto.getPostId();
        log.info("Adding comment to post {} by user {}", postId, username);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)}));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{String.valueOf(username)}));

        Comment comment = createComment(requestDto.getContent(), post, user, null);

        Comment savedComment = commentRepository.save(comment);

        log.info("Comment created with id: {}", savedComment.getId());

        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(savedComment);
        return commentResponseDto;
    }

    @Override
    @Transactional
    public CommentResponseDto replyToComment(ReplyCommentRequestDto requestDto, String username) {
        Long postId = requestDto.getPostId();
        Long parentId = requestDto.getParentId();
        log.info("Adding reply to comment {} on post {} by user {}", parentId, postId, username);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)}));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{username}));
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Comment.ERR_PARENT_COMMENT_NOT_FOUND, new String[]{String.valueOf(parentId)}));

        if (!parent.getPost().getId().equals(postId)) {
            throw new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_COMMENT_IN_POST, new String[]{String.valueOf(parent.getId()), String.valueOf(post.getId())});
        }

        Comment reply = createComment(requestDto.getContent(), post, user, parent);
        Comment savedReply = commentRepository.save(reply);

        log.info("Reply created with id: {}", savedReply.getId());

        return commentMapper.toCommentResponseDto(savedReply);
    }


    @Override
    @Transactional
    public CommentResponseDto updateComment(Long commentId, String content, Long postId, String username) {
        log.debug("Updating comment {} on post {} by user {}", commentId, postId, username);

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_ID, new String[]{String.valueOf(commentId)})
        );

        validateCommentOwnership(comment, postId, username);

        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);

        log.debug("Comment {} updated successfully", commentId);
        return commentMapper.toCommentResponseDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, String username) {

        log.debug("Deleting comment {} on post {} by user {}", commentId, postId, username);

        Comment comment = findCommentWithId(commentId);
        validateCommentOwnership(comment, postId, username);
        commentRepository.delete(comment);

        log.debug("Comment {} hard deleted", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByPost(Long postId, Pageable pageable) {
        log.debug("Getting comments for post {} with lazy loading", postId);
        return commentRepository.findParentCommentsByPostId(postId, pageable)
                .map(comment -> {
                    CommentResponseDto dto = commentMapper.toCommentResponseDto(comment);
                    dto.setReplyCount(comment.getReplies().size());
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
    public CommentResponseDto getCommentWithReplies(Long commentId) {
        log.info("Getting comment with replies for comment {}", commentId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_ID, new String[]{String.valueOf(commentId)})
        );
        CommentResponseDto dto = commentMapper.toCommentResponseDto(comment);
        log.info("Found comment with replies: {}", dto);
        return dto;
    }

    private Comment createComment(String content, Post post, User user, Comment parent) {
        int level = (parent == null) ? 0 : parent.getCommentLevel() + 1;

        Comment newComment = Comment.builder()
                .commentLevel(level)
                .content(content)
                .user(user)
                .parent(parent)
                .post(post)
                .replies(new ArrayList<>())
                .build();

        post.setCommentCount(post.getCommentCount() + 1);
        post.getComments().add(newComment);
        postRepository.save(post);

        return commentRepository.save(newComment);
    }

    private Comment findCommentWithId(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(commentId)}));
        return comment;
    }

    private void validateCommentOwnership(Comment comment, Long postId, String username) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_COMMENT_IN_POST, new String[]{String.valueOf(comment.getId())});
        }
        if (!comment.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException(ErrorMessage.Comment.ERR_NOT_HAVE_PERMISSION);
        }
    }
}