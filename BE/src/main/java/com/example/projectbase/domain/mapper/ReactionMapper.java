package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.request.ReactionRequestDto;
import com.example.projectbase.domain.dto.response.ReactionResponseDto;
import com.example.projectbase.domain.entity.Reaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReactionMapper {

    ReactionResponseDto toReactionResponseDto(Reaction reaction);

}
