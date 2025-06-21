package com.example.projectbase.service;

import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;

import java.util.List;

public interface PostService {
    PostResponseDto createPost(PostRequestDto dto);
    List<PostResponseDto> getAllPosts();
    PostResponseDto getPostById(Long id);
    PostResponseDto updatePost(Long id, PostRequestDto dto);
    void deletePost(Long id);
}

