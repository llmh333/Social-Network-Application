package com.example.projectbase.service;

import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    PostResponseDto createPostWithImage(PostRequestDto dto, MultipartFile image);
    PostResponseDto createPostWithVideo(PostRequestDto dto, MultipartFile video);
    PostResponseDto createPostWithAudio(PostRequestDto dto,
                                        MultipartFile audio, String audioTitle, String category, String singerName);
    PostResponseDto createPostWithMultiImage(PostRequestDto dto,
                                             List<MultipartFile> images);
    PostResponseDto updatePost(Long postId, PostRequestDto dto,
                               MultipartFile image, MultipartFile video, MultipartFile audio,
                               String audioTitle, String category, String singerName,
                               List<MultipartFile> images);
    void deletePost(Long postId);
    PaginationResponseDto<PostResponseDto> getAllPostsByTitleKeyword(PaginationFullRequestDto request);
    PostResponseDto getPostById(Long postId);
}

