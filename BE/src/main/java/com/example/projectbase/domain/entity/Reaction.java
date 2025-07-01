package com.example.projectbase.domain.entity;

import com.example.projectbase.constant.ReactionTypeConstant;
import com.example.projectbase.domain.entity.common.DateAuditing;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reaction")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reaction extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReactionTypeConstant reactionType;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

}
