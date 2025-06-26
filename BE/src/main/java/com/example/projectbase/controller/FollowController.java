package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.RestData;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.FollowRequestDto;
import com.example.projectbase.domain.dto.response.FollowResponseDto;
import com.example.projectbase.domain.dto.response.UserSummaryDto;
import com.example.projectbase.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@RestApiV1
@Log4j2
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping(value = UrlConstant.Follow.EXECUTING_FOLLOW)
    public ResponseEntity<RestData<?>> follow(@RequestBody @Valid FollowRequestDto requestDto) {
        FollowResponseDto response = followService.follow(requestDto);
        return VsResponseUtil.success(response);
    }

    @PostMapping(value = UrlConstant.Follow.UNFOLLOW)
    public ResponseEntity<RestData<?>> unfollow(@RequestBody @Valid FollowRequestDto requestDto) {
        followService.unfollow(requestDto);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT);
    }

    @GetMapping(UrlConstant.Follow.GET_FOLLOWERS)
    public ResponseEntity<RestData<?>> getFollowers(@ParameterObject PaginationRequestDto requestDto) {
        PaginationResponseDto<UserSummaryDto> responseDto = followService.getFollowers(requestDto);
        return VsResponseUtil.success(responseDto);
    }

    @GetMapping(UrlConstant.Follow.GET_FOLLOWINGS)
    public ResponseEntity<RestData<?>> getFollowings(@ParameterObject PaginationRequestDto requestDto) {
        PaginationResponseDto<UserSummaryDto> responseDto = followService.getFollowings(requestDto);
        return VsResponseUtil.success(responseDto);
    }

}
