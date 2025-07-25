package com.example.projectbase.domain.entity;

import com.example.projectbase.constant.VerifyConstant;
import com.example.projectbase.domain.entity.common.DateAuditing;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpForgotPassword extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "otp_code", unique = true, nullable = false)
    private String otpCode;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerifyConstant verified;

    @Column(name = "reset_password_token", unique = true)
    private String resetPasswordToken;

    @Column(name = "reset_password_expiry")
    private LocalDateTime resetPasswordExpiryDate;
}
