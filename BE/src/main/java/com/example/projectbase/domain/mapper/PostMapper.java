package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.entity.Post;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostMapper {
    public PostResponseDto toDto(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .reactionCount(post.getReactionCount())
                .commentCount(post.getCommentCount())
                .createdBy(post.getCreatedBy())
                .createdDate(post.getCreatedDate())
                .lastModifiedDate(post.getLastModifiedDate())
                .mediaList(post.getMediaList().stream()
                        .map(m -> MediaResponseDto.builder()
                                .publicId(m.getPublicId())
                                .url(m.getUrl())
                                .resourceType(m.getResourceType())
                                .format(m.getFormat())
                                .dataSize(m.getDataSize())
                                .createdAt(m.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}