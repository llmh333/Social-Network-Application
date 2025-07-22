package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.dto.request.SharePostRequestDto;
import com.example.projectbase.domain.dto.response.PostSummaryDto;
import com.example.projectbase.domain.dto.response.SharePostResponseDto;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.mapper.MediaMapper;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Override
    public SharePostResponseDto sharePost(Long postId, SharePostRequestDto request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{username}));

        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ORIGINAL_POST));

        originalPost.setShareCount(originalPost.getShareCount() + 1);
        PostSummaryDto postSummaryDto = new PostSummaryDto();
        postSummaryDto.setTitle(originalPost.getTitle());
        postSummaryDto.setContent(originalPost.getContent());
        postSummaryDto.setShareCount(originalPost.getShareCount());
        postSummaryDto.setCreatedBy(originalPost.getCreatedBy());
        postSummaryDto.setMediaList(originalPost.getMediaList().stream().map(mediaMapper::toMediaResponseDto).collect(Collectors.toList()));
        postRepository.save(originalPost);

        Post newPost = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .originalPost(originalPost)
                .mediaType(originalPost.getMediaType())
                .build();
        newPost =  postRepository.save(newPost);
        SharePostResponseDto responseDto = new SharePostResponseDto();
        responseDto.setId(newPost.getId());
        responseDto.setTitle(newPost.getTitle());
        responseDto.setContent(newPost.getContent());
        responseDto.setOriginalPost(postSummaryDto);
        return responseDto;
    }

}
