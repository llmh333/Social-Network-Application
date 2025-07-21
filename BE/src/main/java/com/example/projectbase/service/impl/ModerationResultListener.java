package com.example.projectbase.service.impl;

import com.example.projectbase.constant.PostStatusConstant;
import com.example.projectbase.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class ModerationResultListener {

    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "moderation_results", groupId = "result-handler-group")
    public void handleRejectedPosts(String message) {
        try {
            Map<String, String> result = objectMapper.readValue(message, Map.class);
            if ("REJECTED".equals(result.get("status"))) {
                Long postId = Long.parseLong(result.get("postId"));
                postRepository.findById(postId).ifPresent(post -> {
                    post.setStatus(PostStatusConstant.REJECTED.REJECTED);
                    postRepository.save(post);
                    log.info("Đã cập nhật trạng thái REJECTED cho postId: {}", postId);
                    // (Tùy chọn) Xóa file trên S3
                });
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý kết quả bị từ chối: {}", message, e);
        }
    }
}
