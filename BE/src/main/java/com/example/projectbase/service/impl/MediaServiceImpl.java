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

    @Override
    public MediaResponseDto uploadVideo(MultipartFile multipartFile) throws IOException, InterruptedException {
        validateFile(multipartFile);
        Media videoUpload = mediaPending(multipartFile, "video");
        MediaResponseDto  mediaResponseDto = new MediaResponseDto();
        File videoCompress = videoProcessingService.compressVideo(multipartFile);
        try {
            Map<String, Object> metaData = ObjectUtils.asMap(
                    "resource_type", "video",
                    "quality", "auto",
                    "video_codec", "h264",
                    "chunk_size", 7000000,
                    "eager_async", true,
                    "public_id", videoUpload.getPublicId(),
                    "invalidate", true,
                    "eager", Arrays.asList(
                            new Transformation()
                                    .fetchFormat("m3u8"),
                            new Transformation()
                                    .startOffset("auto")
                                    .width(720)
                                    .height(1080)
                                    .crop("fill")
                                    .gravity("center")
                                    .fetchFormat("jpg")
                    )
            );
            Map<String, Object> result = cloudinary.uploader().uploadLarge(videoCompress, metaData);
            List<Map<String, Object>> eagerList = (List<Map<String, Object>>) result.get("eager");
            if (eagerList != null) {
                for (Map<String, Object> eager : eagerList) {
                    String url = (String) eager.get("secure_url");
                    if (url != null && url.endsWith(".jpg")) {
                        videoUpload.setThumbnailUrl(url);
                    } else if (url != null && url.endsWith(".m3u8")) {
                        videoUpload.setPlaybackUrl(url);
                    }
                }
            }
            videoUpload.setSecureUrl(result.get("secure_url").toString());
            videoUpload.setHeight(Long.parseLong(result.get("height").toString()));
            videoUpload.setWidth(Long.parseLong(result.get("width").toString()));
            videoUpload.setFormat(result.get("format").toString());
            videoUpload.setStatus(UploadStatusConstant.DONE);
            mediaResponseDto = mediaMapper.toMediaResponseDto(mediaRepository.save(videoUpload));
            mediaResponseDto.setAuthorId(videoUpload.getUser().getId());
        } catch (IOException e) {
            videoUpload.setStatus(UploadStatusConstant.ERROR);
            mediaRepository.save(videoUpload);
            videoCompress.delete();
            e.printStackTrace();
        } finally {
            videoCompress.delete();
        }
        return mediaResponseDto;
    }

    @Override
    public MediaResponseDto uploadImage(MultipartFile multipartFile) {
        validateFile(multipartFile);
        MediaResponseDto mediaResponseDto =new MediaResponseDto();
        Media imageUpload = mediaPending(multipartFile,"image");
        try {
            Map<String, Object> metaData = ObjectUtils.asMap(
                    "resource_type", "image",
                    "quality", "auto",
                    "fetch_format", "auto",
                    "public_id", imageUpload.getPublicId()
            );
            Map<String, Object> result = cloudinary.uploader().upload(multipartFile.getBytes(), metaData);
            log.info("result: {}", result);
            imageUpload.setDataSize(multipartFile.getSize());
            imageUpload.setFormat(result.get("format").toString());
            imageUpload.setPublicId(result.get("public_id").toString());
            imageUpload.setSecureUrl(result.get("secure_url").toString());
            imageUpload.setResourceType(result.get("resource_type").toString());
            imageUpload.setWidth(Long.valueOf((Integer) result.get("width")));
            imageUpload.setHeight(Long.valueOf((Integer) result.get("height")));
            imageUpload.setStatus(UploadStatusConstant.DONE);
            Media savedMedia = mediaRepository.save(imageUpload);
            mediaResponseDto = mediaMapper.toMediaResponseDto(savedMedia);
            mediaResponseDto.setAuthorId(imageUpload.getUser().getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mediaResponseDto;
    }

    @Override
    public List<MediaResponseDto> uploadMultiImage(List<MultipartFile> file) {
        List<MediaResponseDto> mediaResponseDtos = new ArrayList<>();
        for (MultipartFile fileItem : file) {
            mediaResponseDtos.add(uploadImage(fileItem));
        }
        return mediaResponseDtos;
    }

    @Override
    public MediaResponseDto uploadAudio(MultipartFile multipartFile, String title, String category, String singerName) throws IOException, InterruptedException {
        validateFile(multipartFile);
        Media audioUpload = mediaPending(multipartFile,"audio");
        MediaResponseDto mediaResponseDto =new MediaResponseDto();
        File audioCompress = videoProcessingService.compressVideo(multipartFile);
        try {
            Map<String, Object> metaData = ObjectUtils.asMap(
                    "resource_type", "video",
                    "quality", "auto",
                    "chunk_size", 7000000,
                    "eager_async", true,
                    "public_id", audioUpload.getPublicId(),
                    "invalidate", true
            );
            Map<String, Object> result = cloudinary.uploader().uploadLarge(audioCompress, metaData);
            log.info("result: {}", result);

            audioUpload.setDataSize(Long.valueOf((Integer) result.get("bytes")));
            audioUpload.setFormat(result.get("format").toString());
            audioUpload.setSecureUrl(result.get("secure_url").toString());
            audioUpload.setTitle(title);
            audioUpload.setCategory(category);
            audioUpload.setSingerName(singerName);
            audioUpload.setStatus(UploadStatusConstant.DONE);
            Media savedMedia = mediaRepository.save(audioUpload);
            mediaResponseDto = mediaMapper.toMediaResponseDto(savedMedia);
            mediaResponseDto.setAuthorId(savedMedia.getUser().getId());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            audioCompress.delete();
        }
        return mediaResponseDto;
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
            log.info("invalidId: {}", invalidPublicId.toString());
            mediaRepository.deleteAllByPublicIdIn(publicIdList);
            for (Media media : mediaList) {
                Map<String, Object> metaData = ObjectUtils.asMap(
                        "resource_type", media.getResourceType(),
                        "invalidated", true
                );
                try {
                    Map<String, String> result = cloudinary.uploader().destroy(media.getPublicId(), metaData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{String.valueOf(invalidPublicId)});
        }
        return true;
    }

    @Override
    public Media uploadAvatar(UserPrincipal principal, MultipartFile file) {
        log.info("[UPLOAD AVATAR] User: {}", principal.getUsername());

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new InvalidException(ErrorMessage.Media.ERR_INVALID_MEDIA_TYPE);
        }
        if (file.getSize() > MediaConstant.MAX_SIZE_IMAGE) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_IMAGE);
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        mediaRepository.findByUserAndType(user, "avatar").ifPresent(old -> {
            try {
                cloudinary.uploader().destroy(old.getPublicId(), ObjectUtils.emptyMap());
                mediaRepository.delete(old);
                log.info("[DELETE OLD AVATAR] {}", old.getPublicId());
            } catch (IOException e) {
                log.error("Failed to delete old avatar from cloudinary", e);
            }
        });

        String publicId = "avatar/" + user.getId() + "_" + System.currentTimeMillis();


        Map<String, Object> metaData = ObjectUtils.asMap(
                "resource_type", "image",
                "quality", "auto",
                "fetch_format", "auto",
                "public_id", publicId
        );

        try {
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), metaData);
            log.info("[UPLOAD AVATAR RESULT] {}", result);

            Media media = Media.builder()
                    .publicId(result.get("public_id").toString())
                    .secureUrl(result.get("secure_url").toString())
                    .format(result.get("format").toString())
                    .resourceType(result.get("resource_type").toString())
                    .dataSize(file.getSize())
                    .width(Long.valueOf((Integer) result.get("width")))
                    .height(Long.valueOf((Integer) result.get("height")))
                    .status(UploadStatusConstant.DONE)
                    .type("avatar")
                    .user(user)
                    .build();

            return mediaRepository.save(media);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
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
                .status(UploadStatusConstant.PROCESSING)
                .user(user)
                .build();
        return mediaRepository.save(media);
    }
}