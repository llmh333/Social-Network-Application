package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.RoleConstant;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final MailServiceImpl mailService;

    @PreAuthorize("#username == authentication.principal.username")
    @Override
    @Transactional
    public CommentResponseDto addComment(Long postId, CommentRequestDto requestDto, String username) {

        log.info("Adding comment to post {} by user {}", postId, username);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)}));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{String.valueOf(username)}));

        User userOfPost= userRepository.findByUsername(post.getCreatedBy())
                .orElseThrow(() -> new  NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{String.valueOf(post.getCreatedBy())}));
        Comment comment = createComment(requestDto.getContent(), post, user, null);

        Comment savedComment = commentRepository.save(comment);

        log.info("Comment created with id: {}", savedComment.getId());
        String content = "Người dùng "+user.getFirstName()+" " + user.getLastName() + " đã bình luận vào một bài viết của bạn";
        mailService.sendEmailWithObject(userOfPost.getEmail(),content,"Thông báo từ Chill And Chill");
        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(savedComment);
        return commentResponseDto;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public CommentResponseDto replyToComment(Long postId, ReplyCommentRequestDto requestDto, String username) {
        Long parentCommentId = requestDto.getParenCommentId();
        log.info("Adding reply to comment {} on post {} by user {}", parentCommentId, postId, username);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)}));

        User userOfPost= userRepository.findByUsername(post.getCreatedBy())
                .orElseThrow(() -> new  NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{String.valueOf(post.getCreatedBy())}));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{username}));
        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Comment.ERR_PARENT_COMMENT_NOT_FOUND, new String[]{String.valueOf(parentCommentId)}));

        if (!parent.getPost().getId().equals(postId)) {
            throw new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_COMMENT_IN_POST, new String[]{String.valueOf(parent.getId()), String.valueOf(post.getId())});
        }

        Comment reply = createComment(requestDto.getContent(), post, user, parent);
        Comment savedReply = commentRepository.save(reply);

        log.info("Reply created with id: {}", savedReply.getId());
        String content = "Người dùng "+user.getFirstName()+" " + user.getLastName() + " đã trả lời một bình luận vào bài viết của bạn";
        mailService.sendEmailWithObject(userOfPost.getEmail(),content,"Thông báo từ Chill And Chill");
        return commentMapper.toCommentResponseDto(savedReply);
    }

    @PreAuthorize("isAuthenticated()")
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

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, String username) {

        log.debug("Deleting comment {} on post {} by user {}", commentId, postId, username);

        Comment comment = findCommentWithId(commentId);
        validateCommentOwnership(comment, postId, username);
        commentRepository.delete(comment);

        log.debug("Comment {} hard deleted", commentId);
    }

    @PreAuthorize("isAuthenticated()")
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

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getRepliesByParentId(Long postId, Long parentId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)}));
        log.debug("Getting replies for parent comment {}", parentId);
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId)
                .stream()
                .map(commentMapper::toCommentResponseDto)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto getCommentWithReplies(Long postId, Long commentId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)}));
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
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{username}));
        if (!currentUser.getRole().getName().equals(RoleConstant.ADMIN)) {
            if (!comment.getPost().getId().equals(postId)) {
                throw new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_COMMENT_IN_POST, new String[]{String.valueOf(comment.getId())});
            }
            if (!comment.getUser().getUsername().equals(username)) {
                throw new UnauthorizedException(ErrorMessage.Comment.ERR_NOT_HAVE_PERMISSION);
            }
        }
    }
}