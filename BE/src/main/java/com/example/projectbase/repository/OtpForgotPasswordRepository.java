package com.example.projectbase.repository;

import com.example.projectbase.domain.entity.OtpForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Repository
public interface OtpForgotPasswordRepository extends JpaRepository<OtpForgotPassword, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpForgotPassword p WHERE (p.expiryDate < :now AND p.verified = com.example.projectbase.constant.VerifyConstant.FALSE) OR (p.resetPasswordExpiryDate < :now)")
    void deleteExpiredOtpCode(@Param("now") LocalDateTime now);

    OtpForgotPassword findByOtpCode(String otpCode);

    OtpForgotPassword findByEmail(String email);

    OtpForgotPassword findByResetPasswordToken(String resetPasswordToken);
}
