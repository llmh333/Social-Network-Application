package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.MediaConstant;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.exception.InvalidException;
import com.example.projectbase.exception.MaxUploadSizeMediaException;
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
import java.util.concurrent.CompletableFuture;

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
        if (multipartFile.getSize() > MediaConstant.MAX_SIZE_VIDEO) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_VIDEO);
        }
        File largeFile = videoProcessingService.compressVideo(multipartFile);
        MediaResponseDto responseDto = mediaService.uploadVideo(multipartFile, largeFile);
        return VsResponseUtil.success(HttpStatus.CREATED, responseDto);
    }

    @Operation(summary = "API Upload Image")
    @PostMapping(UrlConstant.Media.UPLOAD_MEDIA_IMAGE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile.getSize() > MediaConstant.MAX_SIZE_IMAGE) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_IMAGE);
        }
        MediaResponseDto responseDto = mediaService.uploadImage(multipartFile);
        return VsResponseUtil.success(HttpStatus.CREATED, responseDto);
    }

    @Operation(summary = "API Upload Audio")
    @PostMapping(UrlConstant.Media.UPLOAD_MEDIA_AUDIO)
    public ResponseEntity<?> uploadAudio(@RequestParam("file") MultipartFile multipartFile,
                                         @Valid @RequestParam("title") String title,
                                         @Valid @RequestParam("category") String category,
                                         @Valid @RequestParam("singer") String singerName) throws IOException, InterruptedException {
        if (multipartFile.getSize() > MediaConstant.MAX_SIZE_AUDIO) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_AUDIO);
        }
        File largeFile = videoProcessingService.compressAudio(multipartFile);
        MediaResponseDto responseDto = mediaService.uploadAudio(multipartFile, largeFile, title, category, singerName);
        return VsResponseUtil.success(HttpStatus.CREATED, responseDto);
    }

    @Operation(summary = "API Upload Multi Image")
    @PostMapping(UrlConstant.Media.UPLOAD_MULTI_MEDIA_IMAGE)
    public ResponseEntity<?> uploadMultiImage(@RequestParam("file") List<MultipartFile> file) {
        List<MediaResponseDto> mediaResponseDtos = mediaService.uploadMultiImage(file);
        return VsResponseUtil.success(HttpStatus.CREATED, mediaResponseDtos);
    }

    @Operation(summary = "API Get Media")
    @GetMapping(UrlConstant.Media.GET_MEDIA_BY_RESOURCE_TYPE)
    public ResponseEntity<?> getAllMedia(@Valid @ParameterObject PaginationFullRequestDto paginationFullRequestDto) {
        PaginationResponseDto responseDto = mediaService.getAllMedia(paginationFullRequestDto);
        if (responseDto == null) {
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, ErrorMessage.Media.ERR_NOT_FOUND_MEDIA);
        }
        return VsResponseUtil.success(HttpStatus.OK,responseDto);
    }

    @Operation(summary = "API Get Media bàng public ID")
    @GetMapping(UrlConstant.Media.GET_MEDIA_BY_PUBLIC_ID)
    public ResponseEntity<?> getMediaByPublicId(@Valid @PathVariable String publicId) {
        MediaResponseDto responseDto = mediaService.getMediaByPublicId(publicId);
        return VsResponseUtil.success(HttpStatus.OK,responseDto);
    }

    @Operation(summary = "API tìm kiếm nhạc theo title hoặc category")
    @GetMapping(UrlConstant.Media.GET_AUDIO_BY_TITLE_OR_CATEGORY_OR_SINGER)
    public ResponseEntity<?> getAudioByTitleOrCategory(@Valid @ParameterObject PaginationRequestDto paginationRequestDto,
                                                       @RequestParam String keyword) {
        PaginationResponseDto responseDto = mediaService.getAudioByTitleOrCategoryOrSinger(paginationRequestDto, keyword);
        return VsResponseUtil.success(HttpStatus.OK, responseDto);
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
