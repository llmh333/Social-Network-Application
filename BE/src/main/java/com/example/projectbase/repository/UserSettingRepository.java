package com.example.projectbase.repository;

import com.example.projectbase.domain.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.projectbase.domain.entity.User;
import java.util.Optional;



@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    Optional<UserSetting> findByUser(User user);
}
