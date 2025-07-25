package com.example.projectbase.domain.entity;

import com.example.projectbase.constant.CommonConstant;
import lombok.*;
import org.apache.tomcat.jni.Local;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenBlacklist {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token = CommonConstant.BEARER_TOKEN;

    @Column(name = "token_type", nullable = false)
    private String tokenType;

    @Column(nullable = false)
    private String reason;

    @Column(name = "blacklisted_at", nullable = false)
    private LocalDateTime blackListAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @PrePersist
    public void prePersist() {
        if (blackListAt == null) blackListAt = LocalDateTime.now();
        if (expiredAt == null) expiredAt = LocalDateTime.now().plusDays(1);
    }

}
