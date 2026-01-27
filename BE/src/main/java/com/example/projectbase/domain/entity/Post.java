package com.example.projectbase.domain.entity;

import com.example.projectbase.constant.MediaType;
import com.example.projectbase.constant.PostStatusConstant;
import com.example.projectbase.domain.entity.common.DateAuditing;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Post extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "media_category_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_MEDIA_CATEGORY"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PostCategory category;

    @ManyToOne
    @JoinColumn(name = "original_post_id")
    private Post originalPost;

    @Column(name = "media_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Media> mediaList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "reaction_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long reactionCount = 0L;

    @Column(name = "comment_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long commentCount = 0L;

    @Column(name = "share_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long shareCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PostStatusConstant status;

    @PrePersist
    public void prePersist() {
        if (reactionCount == null)
            reactionCount = 0L;
        if (commentCount == null)
            commentCount = 0L;
        if (shareCount == null)
            shareCount = 0L;
    }
}
