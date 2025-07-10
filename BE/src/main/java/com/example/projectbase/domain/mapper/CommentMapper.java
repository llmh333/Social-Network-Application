package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.response.CommentResponseDto;
import com.example.projectbase.domain.dto.response.CommonResponseDto;
import com.example.projectbase.domain.dto.response.UserSummaryDto;
import com.example.projectbase.domain.entity.Comment;
import com.example.projectbase.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {


    List<CommentResponseDto> toCommentResponseDtoList(List<Comment> comments);

    UserSummaryDto toUserSummaryDto(User user);

    default CommentResponseDto toCommentResponseDto(Comment comment) {

        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(comment.getId());
        commentResponseDto.setContent(comment.getContent());
        commentResponseDto.setAuthor(toUserSummaryDto(comment.getUser()));
        commentResponseDto.setReplyCount(comment.getReplies().size());
        commentResponseDto.setCreatedAt(comment.getCreatedAt());
        commentResponseDto.setLastModifiedAt(comment.getLastModifiedAt());
        commentResponseDto.setParentCommentId(comment.getParent() == null ? null : comment.getParent().getId());
        commentResponseDto.setPostId(comment.getPost().getId());
        commentResponseDto.setReplies(toCommentResponseDtoList(comment.getReplies()));

        return commentResponseDto;
    }
}