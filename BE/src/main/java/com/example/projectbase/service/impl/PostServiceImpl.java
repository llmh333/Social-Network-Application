package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.SortByDataConstant;
import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.pagination.PagingMeta;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.PostMapper;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.MediaService;
import com.example.projectbase.service.PostService;
import com.example.projectbase.util.MediaProcessingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Log4j2
@Service
//@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final VideoProcessingService videoProcessingService;
    private final AudioProcessingService audioProcessingService;
    private final ImageProcessingService imageProcessingService;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    @PreAuthorize("isAuthenticated()")
    @Override
    public PostResponseDto createPost(PostRequestDto requestDto, List<File> files, List<String> contentTypeFileList) {
        if (!files.get(0).isFile()) {
            throw new BadRequestException(ErrorMessage.Post.ERR_FILES_NULL);
        }

        if (requestDto.getMediaType() != null) {
            String contentTypeMedia = contentTypeFileList.get(0).split("/")[0];
            log.info("Content type Media: {}", contentTypeMedia);
            if (!contentTypeMedia.equals(requestDto.getMediaType().toString().toLowerCase())) {
                throw new BadRequestException(ErrorMessage.Post.ERR_FILES_INVALID_FORMAT);
            }
        }

        Post post = postRepository.save(buildPostFromDto(requestDto));
        processAndSaveMediaAsync(post, requestDto, files, contentTypeFileList);
        log.info("Post created: {}", post.toString());
        return postMapper.toPostResponseDto(post);

    }

    public CompletableFuture<Void> processAndSaveMediaAsync(Post post, PostRequestDto requestDto, List<File> files, List<String> contentTypeFileList) {
        switch (requestDto.getMediaType()) {
            case IMAGE:
                return imageProcessingService.uploadMultipleImages(files, contentTypeFileList)
                        .thenAccept(imageResponseDtos -> {
                            imageResponseDtos.forEach(mediaDto -> saveMediaToPost(post, mediaDto));
                        });
            case VIDEO:
                if (files.size() != 1) {
                    throw new BadRequestException(ErrorMessage.Media.ERR_VIDEO_NOT_MULTIPLE_NOT_ALLOWED);
                }

                return videoProcessingService.uploadVideo(files.get(0), contentTypeFileList.get(0)).thenAccept(dto -> saveMediaToPost(post, dto));
            case AUDIO:
                if (files.size() != 2) {
                    throw new BadRequestException(ErrorMessage.Media.ERR_AUDIO_UPLOAD_FORMAT);
                }

                return audioProcessingService.uploadAudio(files.get(0), files.get(1), contentTypeFileList, requestDto.getTitle(), requestDto.getSingerName(), requestDto.getCategory())
                        .thenAccept(audioResponseDtos -> {
                            audioResponseDtos.forEach(mediaDto -> saveMediaToPost(post, mediaDto));
                        }
                );
            default:
                return CompletableFuture.completedFuture(null);
        }
    }

    @PreAuthorize("isAuthenticated() and @postServiceImpl.isOwner(#postId, authentication.username)")
    @Transactional
    @Override
    public void deletePost(Long postId) {
        Post post = findPostOrThrow(postId);
        postRepository.delete(post);
    }

    @PreAuthorize("isAuthenticated()")
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
        Post post = findPostOrThrow(postId);
        PostResponseDto postResponseDto = postMapper.toPostResponseDto(post);
        if (post.getOriginalPost() != null) {
            postResponseDto.setOriginalPostId(post.getOriginalPost().getId());
        }
        log.info("List Media size: {}",  post.getMediaList().size());
        return postResponseDto;
    }

    private Post buildPostFromDto(PostRequestDto requestDto) {
        return Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .reactionCount(0L)
                .commentCount(0L)
                .shareCount(0L)
                .mediaType(requestDto.getMediaType())
                .mediaList(new ArrayList<>())
                .build();
    }

    private void saveMediaToPost(Post post, MediaResponseDto dto) {

        if (post.getMediaList() == null) {
            post.setMediaList(new ArrayList<>());
        }

        Media media = mediaRepository.findMediaByPublicId(dto.getPublicId());
        if (media == null) {
            throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{dto.getPublicId()});
        }
        media.setPost(post);
        post.getMediaList().add(media);
        mediaRepository.save(media);
    }

    private Post findPostOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(id)}));
    }

    public boolean isOwner(Long postId, String username) {
        Post post = findPostOrThrow(postId);
        User user = userRepository.findById(post.getCreatedBy())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{String.valueOf(post.getCreatedBy())}));
        return user.getUsername().equals(username);
    }
}
