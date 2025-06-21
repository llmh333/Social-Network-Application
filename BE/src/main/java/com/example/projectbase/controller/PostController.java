package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestApiV1
public class PostController {

    private final PostService postService;

    @Operation(summary = "API Tạo bài viết mới")
    @PostMapping(UrlConstant.Post.CREATE_POST)
    public ResponseEntity<?> createPost(@Valid @RequestBody PostRequestDto dto) {
        PostResponseDto created = postService.createPost(dto);
        return VsResponseUtil.success(HttpStatus.CREATED, created);
    }

    @Operation(summary = "API Lấy danh sách tất cả bài viết")
    @GetMapping(UrlConstant.Post.GET_POSTS)
    public ResponseEntity<?> getAllPosts() {
        List<PostResponseDto> list = postService.getAllPosts();
        return VsResponseUtil.success(list);
    }

    @Operation(summary = "API Lấy thông tin bài viết theo ID")
    @GetMapping(UrlConstant.Post.GET_POST)
    public ResponseEntity<?> getPostById(@PathVariable("id") Long id) {
        PostResponseDto post = postService.getPostById(id);
        return VsResponseUtil.success(post);
    }

    @Operation(summary = "API Cập nhật bài viết theo ID")
    @PutMapping(UrlConstant.Post.UPDATE_POST)
    public ResponseEntity<?> updatePost(
            @PathVariable("id") Long id,
            @Valid @RequestBody PostRequestDto dto) {
        PostResponseDto updated = postService.updatePost(id, dto);
        return VsResponseUtil.success(updated);
    }

    @Operation(summary = "API Xóa bài viết theo ID")
    @DeleteMapping(UrlConstant.Post.DELETE_POST)
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT, null);
    }
}
