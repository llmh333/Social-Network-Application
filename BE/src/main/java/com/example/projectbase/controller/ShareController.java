package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.request.SharePostRequestDto;
import com.example.projectbase.domain.dto.response.ShareMediaResponseDto;
import com.example.projectbase.domain.dto.response.SharePostResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.mapper.PostMapper;
import com.example.projectbase.service.MediaService;
import com.example.projectbase.service.ShareService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.projectbase.base.VsResponseUtil;
import java.security.Principal;


@RestApiV1
@RequiredArgsConstructor
@Validated
@Tag(name = "share", description = "API thực hiện chức năng share post,media")
public class ShareController {
    private final ShareService shareService;
    private final PostMapper postMapper;

    @PostMapping(UrlConstant.Share.SHARE_POST)
    public ResponseEntity<?> sharePost(@RequestBody SharePostRequestDto request, Principal principal) {
        try {
            String createdBy = principal.getName();
            Post sharedPost = shareService.sharePost(request,createdBy);
            SharePostResponseDto response = postMapper.toDto(sharedPost);
            return VsResponseUtil.success(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to share post");
        }
    }


    @GetMapping(UrlConstant.Share.SHARE_MEDIA + "/{id}")
    public ResponseEntity<?> shareMedia(@PathVariable Long id) {
        ShareMediaResponseDto response = shareService.getShareMedia(id);
        return VsResponseUtil.success(response);
    }
}

