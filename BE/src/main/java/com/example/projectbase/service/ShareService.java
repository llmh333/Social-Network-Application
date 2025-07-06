package com.example.projectbase.service;


import com.example.projectbase.domain.dto.request.SharePostRequestDto;
import com.example.projectbase.domain.dto.response.ShareMediaResponseDto;
import com.example.projectbase.domain.dto.response.SharePostResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.Post;


public interface ShareService {
    public SharePostResponseDto sharePost(SharePostRequestDto request);

    public ShareMediaResponseDto getShareMedia(Long id);
}
