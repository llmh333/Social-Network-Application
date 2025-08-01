package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.VerifyConstant;
import com.example.projectbase.domain.dto.request.ConfirmNewPasswordRequestDto;
import com.example.projectbase.domain.dto.response.VerifiedOtpResponseDto;
import com.example.projectbase.domain.entity.OtpForgotPassword;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.OtpForgotPasswordRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.service.MailService;
import com.example.projectbase.service.OtpForgotPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class OtpForgotPasswordServiceImpl implements OtpForgotPasswordService {

    private final OtpForgotPasswordRepository otpForgotPasswordRepository;
    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean sendOtpForgotPassword(String receivedEmail) {
        User user = userRepository.findByEmail(receivedEmail)
                .orElseThrow(()->new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL, new String[]{receivedEmail}));
        String fullName = user.getFirstName() + " " + user.getLastName();
        String otpCode = generateOtpCode(receivedEmail);
        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("fullName", fullName);
        log.info("otpCode: " + otpCode);
        log.info("fullName: " + fullName);
        String htmlContent = templateEngine.process("otp_send_email", context);
        mailService.sendEmailWithObject(receivedEmail,htmlContent, "Hello, this is a message from Chill and Chill");
        OtpForgotPassword otpForgotPassword = OtpForgotPassword.builder()
                .otpCode(otpCode)
                .verified(VerifyConstant.FALSE)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .email(receivedEmail)
                .build();
        otpForgotPasswordRepository.save(otpForgotPassword);
        return true;
    }

    @Override
    public VerifiedOtpResponseDto verifyOtpForgotPassword(String otpCode) {
        log.info("verify otp code: {}", otpCode);
        OtpForgotPassword otpForgotPassword = otpForgotPasswordRepository.findByOtpCode(otpCode);
        if (otpForgotPassword == null) {
            throw new NotFoundException(ErrorMessage.OtpForgotPassword.ERR_NOT_FOUND, new String[]{otpCode});
        }
        if (otpForgotPassword.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_OTP_EXPIRED, new String[]{otpCode});
        }
        String resetPasswordToken = UUID.randomUUID().toString() + System.currentTimeMillis();
        otpForgotPassword.setVerified(VerifyConstant.TRUE);
        otpForgotPassword.setResetPasswordToken(resetPasswordToken);
        otpForgotPassword.setResetPasswordExpiryDate(LocalDateTime.now().plusMinutes(5));
        otpForgotPasswordRepository.save(otpForgotPassword);
        VerifiedOtpResponseDto response = VerifiedOtpResponseDto.builder()
                .resetPasswordToken(resetPasswordToken)
                .resetPasswordExpiryDate(otpForgotPassword.getResetPasswordExpiryDate())
                .build();
        log.info("verified successfully otp code: {}", otpCode);
        return response;
    }

    @Override
    public boolean confirmChangeNewPassword(ConfirmNewPasswordRequestDto requestDto) {
        log.info("Confirming new password");
        OtpForgotPassword otpForgotPassword = otpForgotPasswordRepository.findByResetPasswordToken(requestDto.getResetPasswordToken());
        if (otpForgotPassword == null) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_NOT_VERIFIED);
        }

        if (otpForgotPassword.getResetPasswordExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_CHANGE_PASSWORD_EXPIRED);
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_PASSWORD_NOT_MATCHED);
        }

        User user = userRepository.findByEmail(otpForgotPassword.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL, new String[]{otpForgotPassword.getEmail()}));

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);
        otpForgotPasswordRepository.delete(otpForgotPassword);
        log.info("Reset new password successfully");
        return true;
    }

    private String generateOtpCode(String receivedEmail) {
        OtpForgotPassword otpForgotPassword = otpForgotPasswordRepository.findByEmail(receivedEmail);
        if (otpForgotPassword != null) {
            if (otpForgotPassword.getExpiryDate().isBefore(LocalDateTime.now())) {
                otpForgotPasswordRepository.delete(otpForgotPassword);
            } else {
                throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_DELAY_GET_OTP, new String[]{String.valueOf(otpForgotPassword.getExpiryDate())});
            }
        }
        secureRandom.setSeed(System.currentTimeMillis());
        int otpCode = secureRandom.nextInt(9999);
        return String.format("%04d", otpCode);
    }

    @Scheduled(fixedRate = 15*60*1000)
    private void deleteExpiredOtpCode() {
        LocalDateTime now = LocalDateTime.now();
        otpForgotPasswordRepository.deleteExpiredOtpCode(now);
    }
}
