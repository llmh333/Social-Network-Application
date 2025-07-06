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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RestApiV1
@RequiredArgsConstructor
@Validated
@Tag(name = "API bài viết")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "Tạo bài viết mới (Nên test ở Postman, dưới đây chỉ là mô tả các dữ liệu đầu vào của api)",
            description =
                    "- `data`: thông tin bài viết dưới dạng JSON (application/json)\n" +
                    "- `files`: danh sách file (chỉ 1 video, 1 audio hoặc nhiều ảnh)\n" +
                    "- `audio`: Nếu upload audio thì audio phải ở đầu danh sách file, rồi đến 1 file image"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo bài viết thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping(value = UrlConstant.Post.CREATE_NEW_POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNewPost(
            @Parameter(
                    description = "Thông tin bài viết (JSON)",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostRequestDto.class))
            )
            @Valid @RequestPart("data") PostRequestDto requestDto,

            @Parameter(
                    description = "Danh sách file upload",
                    required = true
            )
            @RequestPart("files") List<MultipartFile> files) throws IOException, InterruptedException {
        PostResponseDto responseDto = postService.createPost(requestDto, files);
        return VsResponseUtil.success(HttpStatus.CREATED, responseDto);
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


    @Operation(summary = "Xóa bài viết theo ID")
    @DeleteMapping(path = UrlConstant.Post.DELETE_POST)
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT);
    }
}