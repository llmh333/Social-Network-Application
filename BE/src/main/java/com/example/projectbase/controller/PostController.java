package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.exception.MaxUploadSizeMediaException;
import com.example.projectbase.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RestApiV1
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Tạo post kèm nhiều ảnh")
    @PostMapping(path = UrlConstant.Post.CREATE_POST_MULTI_IMAGES, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithMultiImage(
            @RequestParam("data") String data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws JsonProcessingException {
        PostRequestDto dto = objectMapper.readValue(data, PostRequestDto.class);
        PostResponseDto result = postService.createPostWithMultiImage(dto, images);
        return VsResponseUtil.success(HttpStatus.CREATED, result);
    }

    @Operation(summary = "Tạo post kèm 1 video")
    @PostMapping(path = UrlConstant.Post.CREATE_POST_VIDEO, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithVideo(
            @RequestParam("data") String data,
            @RequestPart(value = "video", required = false) MultipartFile video
    ) throws JsonProcessingException {
        PostRequestDto dto = objectMapper.readValue(data, PostRequestDto.class);
        PostResponseDto result = postService.createPostWithVideo(dto, video);
        return VsResponseUtil.success(HttpStatus.CREATED, result);
    }

    @Operation(summary = "Tạo post kèm 1 audio")
    @PostMapping(path = UrlConstant.Post.CREATE_POST_AUDIO, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithAudio(
            @RequestParam("data") String data,
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @RequestParam(value = "audioTitle", required = false, defaultValue = "") String audioTitle,
            @RequestParam(value = "category", required = false, defaultValue = "") String category,
            @RequestParam(value = "singerName", required = false, defaultValue = "") String singerName
    ) {
        try {
            PostRequestDto dto = objectMapper.readValue(data, PostRequestDto.class);
            PostResponseDto result = postService.createPostWithAudio(dto, audio, audioTitle, category, singerName);
            return VsResponseUtil.success(HttpStatus.CREATED, result);
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON data format", e);
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, "Invalid data format");
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (MaxUploadSizeMediaException e) {
            log.warn("File size exceeded: {}", e.getMessage());
            return VsResponseUtil.error(HttpStatus.PAYLOAD_TOO_LARGE, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating post with audio", e);
            return VsResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create post with audio");
        }
    }

    @Operation(summary = "Lấy tất cả bài viết theo từ khóa tiêu đề (có phân trang)")
    @GetMapping(UrlConstant.Post.GET_ALL_POST_BY_TITLE)
    public ResponseEntity<?> getAllPostsByTitleKeyword(
            @Valid @ModelAttribute PaginationFullRequestDto request
    ) {
        PaginationResponseDto<PostResponseDto> result = postService.getAllPostsByTitleKeyword(request);
        return VsResponseUtil.success(HttpStatus.OK, result);
    }

    @Operation(summary = "Lấy thông tin bài viết theo ID")
    @GetMapping(path = UrlConstant.Post.GET_POST)
    public ResponseEntity<?> getPostById(@PathVariable("id") Long id) {
        PostResponseDto post = postService.getPostById(id);
        return VsResponseUtil.success(post);
    }

    @Operation(summary = "Cập nhật bài viết, có thể thay đổi media")
    @PutMapping(path = UrlConstant.Post.UPDATE_POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable("id") Long id,
            @RequestParam("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "video", required = false) MultipartFile video,
            @RequestPart(value = "audio", required = false) MultipartFile audio,
            @RequestParam(value = "audioTitle", required = false) String audioTitle,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "singerName", required = false) String singerName,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws JsonProcessingException {
        PostRequestDto dto = objectMapper.readValue(data, PostRequestDto.class);
        PostResponseDto updated = postService.updatePost(
                id, dto, image, video, audio,
                audioTitle, category, singerName, images
        );
        return VsResponseUtil.success(updated);
    }

    @Operation(summary = "Xóa bài viết theo ID")
    @DeleteMapping(path = UrlConstant.Post.DELETE_POST)
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT, null);
    }
}