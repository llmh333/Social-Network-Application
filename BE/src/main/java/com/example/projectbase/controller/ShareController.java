package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.request.SharePostRequestDto;
import com.example.projectbase.domain.dto.response.SharePostResponseDto;
import com.example.projectbase.service.ShareService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.projectbase.base.VsResponseUtil;

import jakarta.validation.Valid;

@RestApiV1
@RequiredArgsConstructor
@Validated
@Tag(name = "share", description = "API thực hiện chức năng share post,media")
public class ShareController {
    private final ShareService shareService;

    @PostMapping(UrlConstant.Share.SHARE_POST)
    public ResponseEntity<?> sharePost(@PathVariable("postId") Long postId,
            @RequestBody @Valid SharePostRequestDto request) {
        SharePostResponseDto responseDto = shareService.sharePost(postId, request);
        return VsResponseUtil.success(responseDto);
    }
}
