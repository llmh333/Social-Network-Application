package com.example.projectbase.domain.entity;

import com.example.projectbase.constant.GenderConstant;
import com.example.projectbase.domain.entity.common.DateAuditing;
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

  @Column(nullable = true, unique = true)
  private String username;

  @Column(nullable = true)
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
  @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_USER_ROLE"))
  private Role role;

  @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Follow> followings = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Follow> followers = new ArrayList<>();

  @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinColumn(name = "setting_id", referencedColumnName = "id")
  private UserSetting userSetting;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public GenderConstant getGender() {
    return gender;
  }

  public void setGender(GenderConstant gender) {
    this.gender = gender;
  }

  public LocalDate getDob() {
    return dob;
  }

  public void setDob(LocalDate dob) {
    this.dob = dob;
  }

  public AuthProvider getProvider() {
    return provider;
  }

  public void setProvider(AuthProvider provider) {
    this.provider = provider;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public List<Follow> getFollowings() {
    return followings;
  }

  public void setFollowings(List<Follow> followings) {
    this.followings = followings;
  }

  public List<Follow> getFollowers() {
    return followers;
  }

  public void setFollowers(List<Follow> followers) {
    this.followers = followers;
  }

  public UserSetting getUserSetting() {
    return userSetting;
  }

  public void setUserSetting(UserSetting userSetting) {
    this.userSetting = userSetting;
  }
}
