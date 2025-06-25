package com.example.projectbase.service;

import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.ReactionRequestDto;
import com.example.projectbase.domain.dto.response.CommonResponseDto;
import com.example.projectbase.domain.dto.response.ReactionResponseDto;

public interface ReactionService {

    public ReactionResponseDto reactionForPost(ReactionRequestDto request, Long postId);

    public boolean cancelReaction(Long postId);

    public PaginationResponseDto getReactionsOfPost(PaginationRequestDto paginationRequestDto, Long postId);
}
