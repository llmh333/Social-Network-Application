package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.MediaType;
import com.example.projectbase.constant.PostStatusConstant;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class MediaProcessingListener {

    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final VideoProcessingService videoProcessingService;
    private final AudioProcessingService audioProcessingService;
    private final ImageProcessingService imageProcessingService;
    private final AwsS3ServiceImpl awsS3Service;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "processing_requests", groupId = "media-processor-group")
    public void handleSafeMediaForProcessing(String message) {
        log.info("Nhận được yêu cầu xử lý media đã được duyệt: {}", message);
        try {
            // 1. Giải mã message
            Map<String, Object> request = objectMapper.readValue(message, Map.class);
            Long postId = Long.parseLong((String) request.get("postId"));
            List<String> contentTypeList = (List<String>) request.get("contentType");
            List<String> s3Urls = (List<String>) request.get("s3Urls");
            String singerName = (String) request.get("singer_name");


            Post post = postRepository.findById(postId).orElseThrow(() ->
                    new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)})
            );
            post.setStatus(PostStatusConstant.PROCESSING);
            postRepository.save(post);

            List<File> filesToProcess = awsS3Service.downloadMultipleFiles(s3Urls);

            processAndSaveMedia(post, filesToProcess, contentTypeList, singerName, post.getCreatedBy());

        } catch (Exception e) {
            log.error("Lỗi nghiêm trọng khi bắt đầu xử lý media cho message: {}", message, e);
        }
    }

    private void processAndSaveMedia(Post post, List<File> files, List<String> contentTypeFileList, String singerName, String userId) {
        MediaType mediaType = post.getMediaType();

        CompletableFuture<Void> processingFuture;

        switch (mediaType) {
            case IMAGE:
                processingFuture = imageProcessingService.uploadMultipleImages(files, contentTypeFileList, userId)
                        .thenAccept(dtos -> dtos.forEach(dto -> saveMediaToPost(post, dto)));
                break;
            case VIDEO:
                processingFuture = videoProcessingService.uploadVideo(files.get(0), contentTypeFileList.get(0), userId)
                        .thenAccept(dto -> saveMediaToPost(post, dto));
                break;
            case AUDIO:
                processingFuture = audioProcessingService.uploadAudio(files.get(0), files.get(1), contentTypeFileList, singerName, userId)
                        .thenAccept(dtos -> dtos.forEach(dto -> saveMediaToPost(post, dto)));
                break;
            default:
                processingFuture = CompletableFuture.completedFuture(null);
        }

        processingFuture.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Xử lý media cho postId {} thất bại.", post.getId(), ex);
                post.setStatus(PostStatusConstant.REJECTED); // Hoặc một trạng thái lỗi khác
            } else {
                log.info("Xử lý media cho postId {} thành công.", post.getId());
                post.setStatus(PostStatusConstant.APPROVED);
            }
            postRepository.save(post);

            files.forEach(File::delete);
        });
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
}
