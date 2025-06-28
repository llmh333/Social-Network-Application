package com.example.projectbase.service.impl;

import com.example.projectbase.constant.SortByDataConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PagingMeta;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.mapper.PostMapper;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.exception.UnauthorizedException;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.MediaService;
import com.example.projectbase.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MediaService mediaService;
    private final PostMapper postMapper;

    @Override
    public PostResponseDto createPostWithMultiImage(PostRequestDto dto, List<MultipartFile> images) {
        final Post post = buildPost(dto);
        if (images != null && !images.isEmpty()) {
            List<MediaResponseDto> dtos = mediaService.uploadMultiImage(images);
            dtos.forEach(mediaDto -> post.getMediaList().add(mapToMediaEntity(mediaDto, post)));
        }
        return postMapper.toPostResponseDto(postRepository.save(post));
    }

    @Override
    public PostResponseDto createPostWithVideo(PostRequestDto dto, MultipartFile video) {
        Post post = buildPost(dto);
        if (video != null && !video.isEmpty()) {
            File conv = convert(video);
            MediaResponseDto mediaDto = mediaService.uploadVideo(video, conv);
            post.getMediaList().add(mapToMediaEntity(mediaDto, post));
        }
        return postMapper.toPostResponseDto(postRepository.save(post));
    }

    @Override
    public PostResponseDto createPostWithAudio(PostRequestDto dto, MultipartFile audio,
                                               String audioTitle, String category, String singerName) {
        Post post = buildPost(dto);

        if (audio != null && !audio.isEmpty()) {
            validateAudioUploadRequest(audio, singerName, audioTitle);
            try {
                File conv = convert(audio);
                MediaResponseDto mediaDto = mediaService.uploadAudio(audio, conv, audioTitle, category, singerName.trim());
                post.getMediaList().add(mapToMediaEntity(mediaDto, post));
            } catch (Exception e) {
                log.error("Audio upload failed. Deleting post id: {}", post.getId(), e);
                throw new RuntimeException("Failed to upload audio: " + e.getMessage(), e);
            }
        }

        return postMapper.toPostResponseDto(postRepository.save(post));
    }

    private void validateAudioUploadRequest(MultipartFile audio, String singerName, String audioTitle) {
        if (audio != null && !audio.isEmpty()) {
            if (singerName == null || singerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Singer name is required when uploading audio");
            }
            if (audioTitle == null || audioTitle.trim().isEmpty()) {
                throw new IllegalArgumentException("Audio title is required when uploading audio");
            }
        }
    }

    @Override
    public PostResponseDto updatePost(Long postId, PostRequestDto dto, MultipartFile image,
                                      MultipartFile video, MultipartFile audio, String audioTitle,
                                      String category, String singerName, List<MultipartFile> images) {
        final Post post = findPostOrThrow(postId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        if (post.getMediaList() != null && !post.getMediaList().isEmpty()) {
            List<String> publicIds = post.getMediaList().stream().map(Media::getPublicId).collect(Collectors.toList());
            mediaService.deleteMedia(publicIds);
            post.getMediaList().clear();
        }

        if (image != null && !image.isEmpty()) {
            MediaResponseDto mediaDto = mediaService.uploadImage(image);
            post.getMediaList().add(mapToMediaEntity(mediaDto, post));
        }
        if (video != null && !video.isEmpty()) {
            File conv = convert(video);
            MediaResponseDto mediaDto = mediaService.uploadVideo(video, conv);
            post.getMediaList().add(mapToMediaEntity(mediaDto, post));
        }
        if (audio != null && !audio.isEmpty()) {
            File convAudio = convert(audio);
            MediaResponseDto mediaDto = mediaService.uploadAudio(audio, convAudio, audioTitle, category, singerName);
            post.getMediaList().add(mapToMediaEntity(mediaDto, post));
        }
        if (images != null && !images.isEmpty()) {
            final List<MediaResponseDto> dtos = mediaService.uploadMultiImage(images);
            dtos.forEach(mediaDto -> post.getMediaList().add(mapToMediaEntity(mediaDto, post)));
        }

        return postMapper.toPostResponseDto(postRepository.save(post));
    }

    @Override
    public void deletePost(Long postId) {
        Post post = findPostOrThrow(postId);
        if (post.getMediaList() != null && !post.getMediaList().isEmpty()) {
            List<String> publicIds = post.getMediaList().stream()
                    .map(Media::getPublicId)
                    .collect(Collectors.toList());
            mediaService.deleteMedia(publicIds);
        }
        postRepository.deleteById(postId);
    }

    @Override
    public PaginationResponseDto<PostResponseDto> getAllPostsByTitleKeyword(PaginationFullRequestDto request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        String keyword = request.getKeyword();

        Sort sort = Sort.by(request.getSortBy(SortByDataConstant.POST));
        sort = Boolean.FALSE.equals(request.getIsAscending()) ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        Page<Post> postPage = (keyword != null && !keyword.isBlank())
                ? postRepository.searchByTitleKeyword(keyword, pageable)
                : postRepository.findAll(pageable);

        List<PostResponseDto> dtoList = postPage.stream()
                .map(postMapper::toPostResponseDto)
                .collect(Collectors.toList());

        PagingMeta meta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(postPage.getTotalPages())
                .sortBy(request.getSortBy())
                .sortType(request.getIsAscending() ? "ASC" : "DESC")
                .totalElements(postPage.getTotalElements())
                .build();

        return new PaginationResponseDto<>(meta, dtoList);
    }

    @Override
    public PostResponseDto getPostById(Long postId) {
        return postMapper.toPostResponseDto(findPostOrThrow(postId));
    }

    private Post buildPost(PostRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdBy(principal.getUsername())
                .reactionCount(0L)
                .commentCount(0L)
                .mediaList(new ArrayList<>())
                .build();
    }

    private Media mapToMediaEntity(MediaResponseDto dto, Post post) {
        return Media.builder()
                .publicId(dto.getPublicId())
                .secureUrl(dto.getSecureUrl())
                .resourceType(dto.getResourceType())
                .format(dto.getFormat())
                .dataSize(dto.getDataSize())
                .title(dto.getTitle())
                .category(dto.getCategory())
                .thumbnailUrl(dto.getThumbnailUrl())
                .height(dto.getHeight())
                .width(dto.getWidth())
                .post(post)
                .build();
    }

    private Post findPostOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post không tồn tại id=" + id));
    }

    private File convert(MultipartFile file) {
        try {
            File conv = File.createTempFile("upload", file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(conv)) {
                fos.write(file.getBytes());
            }
            return conv;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi convert MultipartFile", e);
        }
    }
}
