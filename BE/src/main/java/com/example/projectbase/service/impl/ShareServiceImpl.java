package com.example.projectbase.service.impl;

import com.example.projectbase.domain.dto.request.SharePostRequestDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.exception.ResourceNotFoundException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final PostRepository postRepository;

    private final MediaRepository mediaRepository;

    @Transactional
    public Post sharePost(SharePostRequestDto request, String createdBy) {
        Post originalPost = postRepository.findById(request.getOriginalPostId())
                .orElseThrow(() -> new IllegalArgumentException("Original post not found with id: " + request.getOriginalPostId()));

        Post newPost = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .originalPost(originalPost)
                .createdBy(createdBy)
                .build();

        return postRepository.save(newPost);
    }

    @Override
    public Media getMediaById(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
    }

}
