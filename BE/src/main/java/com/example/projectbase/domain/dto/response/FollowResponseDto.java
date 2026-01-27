package com.example.projectbase.domain.dto.response;

import com.example.projectbase.domain.entity.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponseDto {

    private Long id;
    private String followerId;
    private String followingId;
    private LocalDateTime createdAt;
}
