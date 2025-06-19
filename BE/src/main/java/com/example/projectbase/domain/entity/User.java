package com.example.projectbase.domain.entity;

import com.example.projectbase.constant.GenderConstant;
import com.example.projectbase.domain.entity.common.DateAuditing;
import com.example.projectbase.constant.AuthProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "user")
public class User extends DateAuditing {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
  private String id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  @JsonIgnore
  private String password;

  @Column(nullable = false, unique = true)
  private String email;

  @Nationalized
  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Nationalized
  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private GenderConstant gender;

  @Column(nullable = false)
  private LocalDate dob;

  @Enumerated(EnumType.STRING)
  @Column(name = "auth_provider", nullable = true)
  private AuthProvider provider;          // local, google, facebook

  @Column(name = "provider_id")
  private String providerId;              // Google/Facebook user ID

  @Column(name = "image_url")
  private String imageUrl;

  @ManyToOne
  @JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "FK_USER_ROLE"))
  private Role role;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Media> media = new ArrayList<>();

  @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Follow> followings = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Follow> followers = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "setting_id", referencedColumnName = "id")
  private UserSetting userSetting;
}
