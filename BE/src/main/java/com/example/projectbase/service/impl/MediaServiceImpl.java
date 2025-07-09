package com.example.projectbase.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.MediaConstant;
import com.example.projectbase.constant.SortByDataConstant;
import com.example.projectbase.constant.UploadStatusConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PagingMeta;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.MediaMapper;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.exception.InvalidException;
import com.example.projectbase.exception.MaxUploadSizeMediaException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.MediaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;
    private final VideoProcessingService videoProcessingService;

    @Override
    public PaginationResponseDto<MediaResponseDto> getAllMedia(PaginationFullRequestDto request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        String keyword = request.getKeyword();

        Sort sort = Sort.by(request.getSortBy(SortByDataConstant.MEDIA));
        if (Boolean.FALSE.equals(request.getIsAscending())) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

        Page<Media> mediaPage = mediaRepository.searchMediaByResourceType(keyword, pageable);
        List<MediaResponseDto> responseDtoList = new ArrayList<>();
        for (Media media : mediaPage) {
            MediaResponseDto mediaResponseDto = mediaMapper.toMediaResponseDto(media);
            mediaResponseDto.setAuthorId(media.getUser().getId());
            responseDtoList.add(mediaResponseDto);
        }
        PagingMeta pagingMeta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(mediaPage.getTotalPages())
                .sortBy(request.getSortBy())
                .sortType("")
                .totalElements(mediaPage.stream().count())
                .build();
        return new PaginationResponseDto<>(pagingMeta, responseDtoList);
    }

    @Override
    public MediaResponseDto getMediaByPublicId(String publicId) {
        Media media = mediaRepository.findMediaByPublicId(publicId);
        if (media == null) {
            throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{publicId});
        }
        return mediaMapper.toMediaResponseDto(media);
    }

    @Override
    public PaginationResponseDto<MediaResponseDto> getAudioByTitleOrCategoryOrSinger(PaginationRequestDto paginationRequestDto, String keyword) {
        int pageNum = paginationRequestDto.getPageNum();
        int pageSize = paginationRequestDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Media> mediaPage = mediaRepository.searchByTitleOrCategoryOrSingerName(keyword, pageable);
        List<MediaResponseDto> responseDtoList = new ArrayList<>();
        for (Media media : mediaPage) {
            MediaResponseDto mediaResponseDto = mediaMapper.toMediaResponseDto(media);
            mediaResponseDto.setAuthorId(media.getUser().getId());
            responseDtoList.add(mediaResponseDto);
        }
        PagingMeta pagingMeta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(mediaPage.getTotalPages())
                .totalElements(mediaPage.stream().count())
                .build();
        return new PaginationResponseDto<>(pagingMeta, responseDtoList);
    }

    @Transactional
    @Override
    public boolean deleteMedia(List<String> publicIdList) {
        List<Media> mediaList = mediaRepository.findAllByPublicIdIn(publicIdList);
        log.info("mediaList: {}", mediaList.toString());
        List<String> invalidPublicId = new ArrayList<>();
        for (String publicId : publicIdList) {
            if (!mediaList.stream().anyMatch(media -> media.getPublicId().equals(publicId))) {
                invalidPublicId.add(publicId);
            }
        }
        if (mediaList.isEmpty()) {
            throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{String.valueOf(invalidPublicId)});
        }
        else if (invalidPublicId.isEmpty()) {
            mediaRepository.deleteAllByPublicIdIn(publicIdList);
            for (Media media : mediaList) {
                Map<String, Object> metaData = ObjectUtils.asMap(
                        "resource_type", media.getResourceType(),
                        "invalidated", true
                );
                try {
                    String result = cloudinary.uploader().destroy(media.getPublicId(), metaData).toString();
                    if (media.getResourceType().equals("audio")) {
                        Map<String, Object> metaDataThumbnail = ObjectUtils.asMap(
                                "resource_type", "image",
                                "invalidated", true
                        );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
//                    log.info();
                    e.printStackTrace();
                }
            }
        } else {
            throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{String.valueOf(invalidPublicId)});
        }
        return true;
    }

    private void validateFile(MultipartFile file) {
        log.info("validateFile: {}", file.getContentType());
        String formatFile = file.getContentType();
        if (formatFile == null || (!formatFile.startsWith("video/") && !formatFile.startsWith("image/") && !formatFile.startsWith("audio/"))) {
            throw new InvalidException(ErrorMessage.Media.ERR_INVALID_MEDIA_TYPE);
        }
        if ((formatFile.startsWith("image/") && file.getSize() > MediaConstant.MAX_SIZE_IMAGE)) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_IMAGE);
        }
        if ((formatFile.startsWith("video/") && file.getSize() > MediaConstant.MAX_SIZE_VIDEO)) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_VIDEO);
        }
        if ((formatFile.startsWith("audio/") && file.getSize() > MediaConstant.MAX_SIZE_AUDIO)) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_AUDIO);
        }
    }
    private String generatePublicIdMedia(MultipartFile file, String typeMedia) {
        if (typeMedia.equals("video")) {
            return "video" + System.currentTimeMillis() + file.getName();
        } else if (typeMedia.equals("image")) {
            return "image" + System.currentTimeMillis() + file.getName();
        }
        return "audio" + System.currentTimeMillis() + file.getName();
    }

    private Media mediaPending(MultipartFile multipartFile, String typeMedia) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));
        String publicId = generatePublicIdMedia(multipartFile, typeMedia);
        Media media = Media.builder()
                .publicId(publicId)
                .resourceType(typeMedia)
                .dataSize(multipartFile.getSize())
                .status(UploadStatusConstant.PENDING)
                .user(user)
                .build();
        return mediaRepository.save(media);
    }
}