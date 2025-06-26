package com.example.projectbase.service;

import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.FollowRequestDto;
import com.example.projectbase.domain.dto.response.FollowResponseDto;
import com.example.projectbase.domain.dto.response.UserSummaryDto;

import java.util.List;

public interface FollowService {

    public FollowResponseDto follow(FollowRequestDto requestDto);

    public boolean unfollow(FollowRequestDto requestDto);

    public PaginationResponseDto<UserSummaryDto> getFollowers(PaginationRequestDto requestDto);

    public PaginationResponseDto<UserSummaryDto> getFollowings(PaginationRequestDto requestDto);

}
