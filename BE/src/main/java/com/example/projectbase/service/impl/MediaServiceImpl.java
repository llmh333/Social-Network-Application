package com.example.projectbase.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.SortByDataConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PagingMeta;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.MediaMapper;
import com.example.projectbase.exception.InvalidException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
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

        Page<Media> mediaPage = mediaRepository.searchByKeyWord(keyword, pageable);
        List<MediaResponseDto> mediaPageDto = mediaPage.stream().map(mediaMapper::toMediaResponseDto).collect(Collectors.toList());
        PagingMeta pagingMeta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(mediaPage.getTotalPages())
                .sortBy(request.getSortBy())
                .sortType("")
                .totalElements(mediaPage.stream().count())
                .build();
        return new PaginationResponseDto<>(pagingMeta, mediaPageDto);
    }

    @Override
    public MediaResponseDto uploadVideo(MultipartFile multipartFile, File file) {
        validateFile(multipartFile);
        try {
            String publicId = generatePublicIdMedia(multipartFile, "video");
            Map<String, Object> metaData = ObjectUtils.asMap(
                    "resource_type", "video",
                    "quality", "auto",
                    "video_codec", "h264",
                    "chunk_size", 7000000,
                    "eager_async", true,
                    "public_id", publicId,
                    "streaming_profile", "hd",
                    "invalidate", true,
                    "eager", Arrays.asList(
                            new Transformation()
                                    .fetchFormat("m3u8"),
                            new Transformation()
                                    .width(1080)
                                    .height(1920)
                                    .crop("fill")
                                    .gravity("center")
                                    .quality("auto:good")
                                    .videoCodec("h264")
                                    .audioCodec("aac")
                                    .fetchFormat("auto"),
                            new Transformation()
                                    .startOffset("auto")
                                    .width(1080)
                                    .height(1920)
                                    .crop("fill")
                                    .gravity("center")
                                    .fetchFormat("jpg")
                    )
            );
            Map<String, Object> result = cloudinary.uploader().uploadLarge(file, metaData);
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(result);
            JsonNode jsonNode = mapper.readTree(json);
            JsonNode eagerNode = jsonNode.get("eager");
            String thumbnailUrl = null;
            if (eagerNode != null && eagerNode.isArray()) {
                for (JsonNode node : eagerNode) {
                    String url = node.get("url").asText();
                    System.out.println(url);
                    if (url.endsWith(".jpg")) {
                        thumbnailUrl = url;
                        break;
                    }
                }
            }
            log.info("result: {}", result);
            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));
            Media media = Media.builder()
                    .dataSize(Long.valueOf((Integer) result.get("bytes")))
                    .format(result.get("format").toString())
                    .user(user)
                    .publicId(jsonNode.get("public_id").asText())
                    .secureUrl(jsonNode.get("secure_url").asText())
                    .resourceType(jsonNode.get("resource_type").asText())
                    .playbackUrl(jsonNode.get("playback_url").asText())
                    .width(Long.valueOf((Integer) result.get("width")))
                    .height(Long.valueOf((Integer) result.get("height")))
                    .thumbnailUrl(thumbnailUrl)
                    .build();
            Media savedMedia = mediaRepository.save(media);
            MediaResponseDto mediaResponseDto = mediaMapper.toMediaResponseDto(savedMedia);
            mediaResponseDto.setAuthorId(user.getId());
            return mediaResponseDto;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            file.delete();
        }
        return null;
    }

    @Override
    public MediaResponseDto uploadImage(MultipartFile file) {
        validateFile(file);
        try {
            String publicId = generatePublicIdMedia(file, "image");
            Map<String, Object> metaData = ObjectUtils.asMap(
                    "resource_type", "image",
                    "quality", "auto",
                    "fetch_format", "auto",
                    "public_id", publicId
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), metaData);
            log.info("result: {}", result);
            UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));
            Media media = Media.builder()
                    .dataSize(file.getSize())
                    .format(result.get("format").toString())
                    .user(user)
                    .publicId(result.get("public_id").toString())
                    .secureUrl(result.get("secure_url").toString())
                    .resourceType(result.get("resource_type").toString())
                    .width(Long.valueOf((Integer) result.get("width")))
                    .height(Long.valueOf((Integer) result.get("height")))
                    .build();
            Media savedMedia = mediaRepository.save(media);
            MediaResponseDto mediaResponseDto = mediaMapper.toMediaResponseDto(savedMedia);
            mediaResponseDto.setAuthorId(user.getId());
            return mediaResponseDto;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MediaResponseDto> uploadMultiImage(List<MultipartFile> file) {
        List<MediaResponseDto> mediaResponseDtos = new ArrayList<>();
        for (MultipartFile fileItem : file) {
            mediaResponseDtos.add(uploadImage(fileItem));
        }
        return mediaResponseDtos;
    }

    @Transactional
    @Override
    public boolean deleteMedia(List<String> publicIdList) {
        try {
            List<Media> mediaList = mediaRepository.findAllByPublicIdIn(publicIdList);
            if (mediaList.isEmpty()) {
                throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{String.valueOf(publicIdList)});
            }
            List<Long> invalidId = new ArrayList<>();
            for (Media media : mediaList) {
                if (!publicIdList.contains(media.getPublicId())) {
                    invalidId.add(media.getId());
                }
            }
            if (invalidId.isEmpty()) {
                mediaRepository.deleteAllByPublicIdIn(publicIdList);
                for (Media media : mediaList) {
                    Map<String, Object> metaData = ObjectUtils.asMap(
                            "resource_type", media.getResourceType(),
                            "invalidated", true
                    );
                    Map<String, String> result = cloudinary.uploader().destroy(media.getPublicId(), metaData);
                }
            } else {
                throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{String.valueOf(invalidId)});
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void validateFile(MultipartFile file) {
        log.info("validateFile: {}", file.getContentType());
        String formatFile = file.getContentType();
        if (formatFile == null || (!formatFile.startsWith("video/") && !formatFile.startsWith("image/"))) {
            throw new InvalidException(ErrorMessage.Media.ERR_INVALID_MEDIA_TYPE);
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
}