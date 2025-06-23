package com.example.projectbase.domain.entity;

import com.example.projectbase.domain.entity.common.DateAuditing;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private String title;

    private String category;

    @Column(name = "singer_name")
    private String singerName;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "secret_url", nullable = false)
    private String secureUrl;

    @Column(name = "playback_url")
    private String playbackUrl;

    @Column(name = "resource_type", nullable = false)
    private String resourceType;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "format", nullable = false)
    private String format;

    @Column(name = "data_size", nullable = false)
    private Long dataSize;

    private Long height;

    private Long width;

    @Column(name = "create_by")
    private LocalDateTime createdBy;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_MEDIA_USER"))
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_MEDIA_POT"))
    private Post post;

    @PrePersist
    @PreUpdate
    private void validateMediaConstraints() {
        if (resourceType.equals("audio")) {
            if (category== null || category.trim().isEmpty()) {
                throw new IllegalArgumentException("Category is required for audio media");
            }
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title is required for audio media");
            }
            if (singerName == null || singerName.trim().isEmpty()) {
                throw new IllegalArgumentException("Singer name is required for audio media");
            }
        }
    }
}
