package com.example.projectbase.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.time.Duration;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.reply-topic}")
    private String replyTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> pf,
            ConcurrentMessageListenerContainer<String, String> repliesContainer) {
        ReplyingKafkaTemplate<String, String, String> template = new ReplyingKafkaTemplate<>(pf, repliesContainer);
//        template.setDefaultTopic(replyTopic);
        template.setDefaultReplyTimeout(Duration.ofSeconds(180));
        return template;
    }

    // Bean này tạo ra container để lắng nghe trên topic phản hồi
    @Bean
    public ConcurrentMessageListenerContainer<String, String> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {
        ConcurrentMessageListenerContainer<String, String> repliesContainer =
                containerFactory.createContainer(replyTopic);
        repliesContainer.getContainerProperties().setGroupId(groupId);
        repliesContainer.setAutoStartup(false);
        return repliesContainer;
    }
}
