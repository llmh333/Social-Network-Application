package com.example.projectbase.service.impl;

import com.example.projectbase.domain.dto.request.SharePostRequestDto;
import com.example.projectbase.domain.dto.response.PostSummaryDto;
import com.example.projectbase.domain.dto.response.ShareMediaResponseDto;
import com.example.projectbase.domain.dto.response.SharePostResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.mapper.MediaMapper;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final PostRepository postRepository;

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;

    @Transactional
    public SharePostResponseDto sharePost(SharePostRequestDto request) {
        Post originalPost = postRepository.findById(request.getOriginalPostId())
                .orElseThrow(() -> new NotFoundException("Original post not found with id: " + request.getOriginalPostId()));

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

    @Override
    public ShareMediaResponseDto getShareMedia(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media not found with id: " + id));

        String downloadUrl = media.getPlaybackUrl();
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            downloadUrl = media.getSecureUrl();
        }

        return ShareMediaResponseDto.builder()
                .id(media.getId())
                .title(media.getTitle())
                .singerName(media.getSingerName())
                .downloadUrl(downloadUrl)
                .build();
    }

}
