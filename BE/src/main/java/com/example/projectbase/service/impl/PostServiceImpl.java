package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.PostMapper;
import com.example.projectbase.exception.InvalidException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.MediaService;
import com.example.projectbase.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MediaService mediaService;
    private final PostMapper postMapper;
    private final UserRepository userRepository;

    @Override
    public PostResponseDto createPost(PostRequestDto dto, List<MultipartFile> files) {
        User user = getCurrentUser();

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .createdBy(user.getUsername())
                .user(user)
                .build();

        if (files != null && !files.isEmpty()) {
            post.setMediaList(processUploads(post, files));
        }

        Post saved = postRepository.save(post);
        return postMapper.toPostResponseDto(saved);
    }

    @Override
    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(postMapper::toPostResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID));
        return postMapper.toPostResponseDto(post);
    }

    @Override
    public PostResponseDto updatePost(Long id, PostRequestDto dto, List<MultipartFile> files) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID));

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        if (files != null && !files.isEmpty()) {
            List<String> oldIds = post.getMediaList().stream()
                    .map(Media::getPublicId)
                    .collect(Collectors.toList());
            mediaService.deleteMedia(oldIds);

            post.getMediaList().clear();
            post.setMediaList(processUploads(post, post.getMediaList(), files));
        }

        Post updated = postRepository.save(post);
        return postMapper.toPostResponseDto(updated);
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID));

        List<String> publicIds = post.getMediaList().stream()
                .map(Media::getPublicId)
                .collect(Collectors.toList());
        mediaService.deleteMedia(publicIds);
        postRepository.delete(post);
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));
    }

    private List<Media> processUploads(Post post, List<MultipartFile> files) {
        return files.stream()
                .map(file -> {
                    MediaResponseDto resp;
                    String type = Objects.requireNonNull(file.getContentType());
                    if (type.startsWith("image")) {
                        resp = mediaService.uploadImage(file);
                    } else if (type.startsWith("video")) {
                        File tmp = convertToTempFile(file);
                        resp = mediaService.uploadVideo(file, tmp);
                    } else if (type.startsWith("audio")) {
                        resp = mediaService.uploadAudio(file);
                    } else {
                        throw new InvalidException(ErrorMessage.Media.ERR_INVALID_MEDIA_TYPE);
                    }

                    Media m = Media.builder()
                            .publicId(resp.getPublicId())
                            .secureUrl(resp.getSecureUrl())
                            .resourceType(resp.getResourceType())
                            .thumbnailUrl(resp.getThumbnailUrl())
                            .width(resp.getWidth())
                            .height(resp.getHeight())
                            .format(resp.getFormat())
                            .dataSize(resp.getDataSize())
                            .user(post.getUser())
                            .post(post)
                            .build();
                    return m;
                })
                .collect(Collectors.toList());
    }

    private File convertToTempFile(MultipartFile file) {
        try {
            File temp = File.createTempFile("upload-", "-" + file.getOriginalFilename());
            try (FileOutputStream out = new FileOutputStream(temp)) {
                out.write(file.getBytes());
            }
            return temp;
        } catch (IOException e) {
            throw new InvalidException("Không thể chuyển MultipartFile sang File tạm", e);
        }
    }
}
