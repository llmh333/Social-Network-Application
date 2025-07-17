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
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.mapper.MediaMapper;
import com.example.projectbase.exception.InvalidException;
import com.example.projectbase.exception.MaxUploadSizeMediaException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.security.CurrentUser;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.MediaService;
import com.example.projectbase.service.impl.VideoProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestApiV1
@Validated
@RequiredArgsConstructor
@Tag(name = "API media", description = "Các API liên quan về upload, get, delete media")
public class MediaController {

    private final MediaService mediaService;

    @Operation(summary = "API Delete Media")
    @DeleteMapping(UrlConstant.Media.DELETE_MEDIA)
    public ResponseEntity<?> deleteMedia(@RequestParam("mediaId") List<String> mediaId) {
        if (mediaService.deleteMedia(mediaId)) {
            return VsResponseUtil.success(HttpStatus.NO_CONTENT);
        }
        return VsResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.ERR_EXCEPTION_GENERAL);
    }
}
