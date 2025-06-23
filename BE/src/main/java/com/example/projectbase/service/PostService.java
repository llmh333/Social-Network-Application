package com.example.projectbase.service;

import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    PostResponseDto createPost(PostRequestDto dto);
    MediaResponseDto postImage(Long postId, MultipartFile file);
    MediaResponseDto postVideo(Long postId, MultipartFile file);
    MediaResponseDto postAudio(Long postId, MultipartFile file, String title, String category, String singerName);
    List<MediaResponseDto> postMultiImage(Long postId, List<MultipartFile> files);
    PostResponseDto updatePost(Long postId, PostRequestDto dto);
    void deletePost(Long postId);
    Page<PostResponseDto> getAllPosts(int page, int size);
    PostResponseDto getPostById(Long postId);
}

