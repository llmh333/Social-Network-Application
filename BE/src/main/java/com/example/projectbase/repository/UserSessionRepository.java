package com.example.projectbase.repository;

import com.cloudinary.AccessControlRule;
import com.example.projectbase.domain.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    UserSession findByUsername(String username);

    UserSession findByToken(String token);

    List<UserSession> findAllByUsername(String username);

    UserSession findByIpAddress(String ipAddress);

    UserSession findByIpAddressAndUsername(String ipAddress, String username);

    void deleteAllByUsername(String username);

    UserSession findByRefreshToken(String refreshToken);

    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false WHERE us.username IN :usernames")
    void deactivateUsers(@Param("usernames") List<String> usernames);
}
