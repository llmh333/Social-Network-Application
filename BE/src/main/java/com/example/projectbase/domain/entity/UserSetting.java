package com.example.projectbase.domain.entity;

import com.example.projectbase.constant.LanguageSetting;
import com.example.projectbase.constant.ThemeSetting;
import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "user_setting")
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ThemeSetting theme;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LanguageSetting language;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
