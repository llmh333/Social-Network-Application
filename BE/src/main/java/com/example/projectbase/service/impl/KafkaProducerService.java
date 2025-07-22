package com.example.projectbase.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Gửi một tin nhắn đến một topic Kafka cụ thể.
     * @param topic Tên của topic.
     * @param message Nội dung tin nhắn.
     */
    public void sendMessage(String topic, String message) {
        log.info("Send message to topic {}", topic);
        kafkaTemplate.send(topic, message);
    }
}
