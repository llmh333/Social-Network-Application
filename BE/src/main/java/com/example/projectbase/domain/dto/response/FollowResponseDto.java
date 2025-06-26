package com.example.projectbase.domain.dto.response;

import com.example.projectbase.domain.entity.User;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
