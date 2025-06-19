package com.example.projectbase.domain.entity;

import com.example.projectbase.domain.entity.common.DateAuditing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "media")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Media extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_MEDIA_USER"))
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_MEDIA_POT"))
    private Post post;
}
