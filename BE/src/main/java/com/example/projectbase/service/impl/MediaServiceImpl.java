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


    @Override
    public MediaResponseDto getMediaByPublicId(String publicId) {
        Media media = mediaRepository.findMediaByPublicId(publicId);
        if (media == null) {
            throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{publicId});
        }
        return mediaMapper.toMediaResponseDto(media);
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
                    e.printStackTrace();
                }
            }
        } else {
            throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{String.valueOf(invalidPublicId)});
        }
        return true;
    }
}