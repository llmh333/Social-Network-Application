package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.response.CommentResponseDto;
import com.example.projectbase.domain.dto.response.UserSummaryDto;
import com.example.projectbase.domain.entity.Comment;
import com.example.projectbase.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "post.id",   target = "postId")
    @Mapping(source = "parent.id", target = "parentCommentId")
    CommentResponseDto toCommentResponseDto(Comment comment);

    List<CommentResponseDto> toCommentResponseDtoList(List<Comment> comments);

    UserSummaryDto toUserSummaryDto(User user);
}