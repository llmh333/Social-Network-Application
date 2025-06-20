package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.entity.Post;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface PostMapper {

    Post toEntity(PostRequestDto dto);

    PostResponseDto toDto(Post entity);

    default Instant map(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneOffset.UTC).toInstant();
    }
}
