package com.example.projectbase.domain.entity;

import com.example.projectbase.domain.entity.common.DateAuditing;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Media extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "secret_url", nullable = false)
    private String secretUrl;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "format", nullable = false)
    private String format;

    @Column(name = "data_size", nullable = false)
    private Long dataSize;

    @Column(name = "create_by")
    private LocalDateTime createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
}
