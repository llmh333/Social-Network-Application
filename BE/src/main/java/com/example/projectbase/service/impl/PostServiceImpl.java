package com.example.projectbase.service.impl;

import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.mapper.PostMapper;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.service.MediaService;
import com.example.projectbase.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    @Autowired private PostRepository postRepository;
    @Autowired private MediaService mediaService;
    @Autowired private PostMapper postMapper;

    @Override
    public PostResponseDto createPost(PostRequestDto dto) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();
        post = postRepository.save(post);
        return postMapper.toDto(post);
    }

    @Override
    public MediaResponseDto postImage(Long postId, MultipartFile file) {
        Post post = findPostOrThrow(postId);
        MediaResponseDto mediaDto = mediaService.uploadImage(file);
        Media m = buildMediaEntity(mediaDto);
        m.setPost(post);
        post.getMediaList().add(m);
        postRepository.save(post);
        return mediaDto;
    }

    @Override
    public MediaResponseDto postVideo(Long postId, MultipartFile file) {
        Post post = findPostOrThrow(postId);
        File conv = convert(file);
        MediaResponseDto mediaDto = mediaService.uploadVideo(file, conv);
        Media m = buildMediaEntity(mediaDto);
        m.setPost(post);
        post.getMediaList().add(m);
        postRepository.save(post);
        return mediaDto;
    }

    @Override
    public MediaResponseDto postAudio(Long postId, MultipartFile file, String title, String category, String singerName) {
        Post post = findPostOrThrow(postId);
        File conv = convert(file);
        MediaResponseDto mediaDto = mediaService.uploadAudio(file, conv, title, category, singerName);
        Media m = buildMediaEntity(mediaDto);
        m.setPost(post);
        post.getMediaList().add(m);
        postRepository.save(post);
        return mediaDto;
    }

    @Override
    public List<MediaResponseDto> postMultiImage(Long postId, List<MultipartFile> files) {
        Post post = findPostOrThrow(postId);
        List<MediaResponseDto> dtos = mediaService.uploadMultiImage(files);
        List<Media> entities = dtos.stream()
                .map(this::buildMediaEntity)
                .peek(m -> m.setPost(post))
                .collect(Collectors.toList());
        post.getMediaList().addAll(entities);
        postRepository.save(post);
        return dtos;
    }

    @Override
    public PostResponseDto updatePost(Long postId, PostRequestDto dto) {
        Post post = findPostOrThrow(postId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        return postMapper.toDto(postRepository.save(post));
    }

    @Override
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException("Post không tồn tại id=" + postId);
        }
        postRepository.deleteById(postId);
    }

    @Override
    public Page<PostResponseDto> getAllPosts(int page, int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return postRepository.findAll(p).map(postMapper::toDto);
    }

    @Override
    public PostResponseDto getPostById(Long postId) {
        return postMapper.toDto(findPostOrThrow(postId));
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

    private Media buildMediaEntity(MediaResponseDto dto) {
        return Media.builder()
                .publicId(dto.getPublicId())
                .url(dto.getUrl())
                .resourceType(dto.getResourceType())
                .format(dto.getFormat())
                .dataSize(dto.getDataSize())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
