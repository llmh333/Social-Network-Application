package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.response.FollowResponseDto;
import com.example.projectbase.domain.entity.Follow;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    FollowResponseDto toFollowResponseDto(Follow follow);
}
