package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.exception.InvalidException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.service.MediaService;
import com.example.projectbase.service.impl.VideoProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestApiV1
@Validated
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final VideoProcessingService videoProcessingService;

    @Operation(summary = "API Upload Video")
    @PostMapping(value = UrlConstant.Media.UPLOAD_MEDIA_VIDEO)
    public ResponseEntity<?> uploadVideo(@RequestParam("file") MultipartFile multipartFile) throws IOException, InterruptedException {
        File largeFile = videoProcessingService.compressVideo(multipartFile);
        MediaResponseDto responseDto = mediaService.uploadVideo(multipartFile, largeFile);
        return VsResponseUtil.success(HttpStatus.CREATED, responseDto);
    }

    @Operation(summary = "API Upload Image")
    @PostMapping(UrlConstant.Media.UPLOAD_MEDIA_IMAGE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        MediaResponseDto responseDto = mediaService.uploadImage(file);
        return VsResponseUtil.success(HttpStatus.CREATED, responseDto);
    }

    @Operation(summary = "API Upload Multi Image")
    @PostMapping(UrlConstant.Media.UPLOAD_MULTI_MEDIA_IMAGE)
    public ResponseEntity<?> uploadMultiImage(@RequestParam("file") List<MultipartFile> file) {
        List<MediaResponseDto> mediaResponseDtos = mediaService.uploadMultiImage(file);
        return VsResponseUtil.success(HttpStatus.CREATED, mediaResponseDtos);
    }
    @Operation(summary = "API Get Media")
    @GetMapping(UrlConstant.Media.GET_MEDIAS)
    public ResponseEntity<?> getAllMedia(@Valid @ParameterObject PaginationFullRequestDto paginationFullRequestDto) {
        PaginationResponseDto responseDto = mediaService.getAllMedia(paginationFullRequestDto);
        if (responseDto == null) {
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, ErrorMessage.Media.ERR_NOT_FOUND_MEDIA);
        }
        return VsResponseUtil.success(HttpStatus.OK,responseDto);
    }

    @Operation(summary = "API Delete Media")
    @DeleteMapping(UrlConstant.Media.DELETE_MEDIA)
    public ResponseEntity<?> deleteMedia(@RequestParam("mediaId") List<String> mediaId) {
        if (mediaService.deleteMedia(mediaId)) {
            return VsResponseUtil.success(HttpStatus.NO_CONTENT);
        }
        return VsResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.ERR_EXCEPTION_GENERAL);
    }
}
