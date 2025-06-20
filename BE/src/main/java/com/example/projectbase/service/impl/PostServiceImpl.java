package com.example.projectbase.service.impl;

import com.example.projectbase.domain.dto.request.PostRequestDto;
import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.mapper.PostMapper;
import com.example.projectbase.exception.ResourceNotFoundException;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Override
    public PostResponseDto createPost(PostRequestDto dto) {
        Post entity = postMapper.toEntity(dto);
        Post saved = postRepository.save(entity);
        return postMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return postMapper.toDto(post);
    }

    @Override
    public PostResponseDto updatePost(Long id, PostRequestDto dto) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        Post toUpdate = postMapper.toEntity(dto);
        toUpdate.setId(id);
        toUpdate.setReactionCount(existing.getReactionCount());
        toUpdate.setCommentCount(existing.getCommentCount());

        Post saved = postRepository.save(toUpdate);
        return postMapper.toDto(saved);
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        postRepository.delete(post);
    }
}
