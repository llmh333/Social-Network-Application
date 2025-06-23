package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RestApiV1
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;

    @Operation(summary = "Tạo post kèm 1 ảnh")
    @PostMapping(path = UrlConstant.Post.CREATE_POST_IMAGE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithImage(
            @RequestPart("data") @Valid PostRequestDto dto,
            @RequestPart("image") MultipartFile image
    ) {
        PostResponseDto resp = postService.createPostWithImage(dto, image);
        return VsResponseUtil.success(HttpStatus.CREATED, resp);
    }

    @Operation(summary = "Tạo post kèm 1 video")
    @PostMapping(path = UrlConstant.Post.CREATE_POST_VIDEO, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithVideo(
            @RequestPart("data") @Valid PostRequestDto dto,
            @RequestPart("video") MultipartFile video
    ) {
        PostResponseDto resp = postService.createPostWithVideo(dto, video);
        return VsResponseUtil.success(HttpStatus.CREATED, resp);
    }

    @Operation(summary = "Tạo post kèm 1 audio")
    @PostMapping(path = UrlConstant.Post.CREATE_POST_AUDIO, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithAudio(
            @RequestPart("data") @Valid PostRequestDto dto,
            @RequestPart("audio") MultipartFile audio
    ) {
        PostResponseDto resp = postService.createPostWithAudio(dto, audio);
        return VsResponseUtil.success(HttpStatus.CREATED, resp);
    }

    @Operation(summary = "Tạo post kèm nhiều ảnh")
    @PostMapping(path = UrlConstant.Post.CREATE_POST_MULTI_IMAGES, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithMultiImage(
            @RequestPart("data") @Valid PostRequestDto dto,
            @RequestPart("images") List<MultipartFile> images
    ) {
        PostResponseDto resp = postService.createPostWithMultiImage(dto, images);
        return VsResponseUtil.success(HttpStatus.CREATED, resp);
    }

    @Operation(summary = "Lấy danh sách tất cả bài viết (có phân trang)")
    @GetMapping(path = UrlConstant.Post.GET_POSTS)
    public ResponseEntity<?> getAllPosts(@ModelAttribute PaginationFullRequestDto paginationRequest) {
        PaginationResponseDto<PostResponseDto> page = postService.getAllPosts(paginationRequest);
        return VsResponseUtil.success(HttpStatus.OK, page);
    }

    @Operation(summary = "Lấy thông tin bài viết theo ID")
    @GetMapping(path = UrlConstant.Post.GET_POST)
    public ResponseEntity<?> getPostById(@PathVariable("id") Long id) {
        PostResponseDto post = postService.getPostById(id);
        return VsResponseUtil.success(post);
    }

    @Operation(summary = "Cập nhật bài viết theo ID")
    @PutMapping(path = UrlConstant.Post.UPDATE_POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable("id") Long id,
            @RequestPart("data") @Valid PostRequestDto dto,
            @RequestPart("files") List<MultipartFile> files
    ) {
        PostResponseDto updated = postService.updatePost(id, dto, files);
        return VsResponseUtil.success(updated);
    }

    @Operation(summary = "Xóa bài viết theo ID")
    @DeleteMapping(path = UrlConstant.Post.DELETE_POST)
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT, null);
    }
}