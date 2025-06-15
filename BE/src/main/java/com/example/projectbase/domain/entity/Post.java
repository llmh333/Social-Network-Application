package com.example.projectbase.domain.entity;

import com.example.projectbase.domain.entity.common.DateAuditing;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> mediaList = new ArrayList<>();

    @Column(name = "reaction_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long reactionCount = 0L;

    @Column(name = "comment_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long commentCount = 0L;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

}
