package com.example.projectbase.service;

import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PaginationSortRequestDto;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface PostService {

    PostResponseDto createPost(PostRequestDto requestDto, List<File> files, List<MultipartFile> multipartFiles, List<String> contentTypeList) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException;

    void deletePost(Long postId);

    PaginationResponseDto<PostResponseDto> getAllPostsByTitleKeyword(PaginationFullRequestDto request);

    PaginationResponseDto<PostResponseDto> getFavoritePosts(PaginationSortRequestDto request);

    PostResponseDto getPostById(Long postId);

    PaginationResponseDto<PostResponseDto> getPostsTrendingForUser(PaginationFullRequestDto request) throws JsonProcessingException;
}

