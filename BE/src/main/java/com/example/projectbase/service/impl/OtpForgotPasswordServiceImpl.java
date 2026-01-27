package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.dto.request.ConfirmNewPasswordRequestDto;
import com.example.projectbase.domain.dto.response.VerifiedOtpResponseDto;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.service.MailService;
import com.example.projectbase.service.OtpForgotPasswordService;
import com.example.projectbase.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class OtpForgotPasswordServiceImpl implements OtpForgotPasswordService {

    private final MailService mailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    @Override
    public boolean sendOtpForgotPassword(String receivedEmail) {
        User user = userRepository.findByEmail(receivedEmail)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL,
                        new String[] { receivedEmail }));
        String fullName = user.getFirstName() + " " + user.getLastName();
        String otpCode = generateOtpCode(receivedEmail);
        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("fullName", fullName);
        log.info("otpCode: " + otpCode);
        log.info("fullName: " + fullName);
        String htmlContent = templateEngine.process("otp_send_email", context);
        mailService.sendEmailWithObject(receivedEmail, htmlContent, "Hello, this is a message from Chill and Chill");

        // Save OTP to Redis with 5 minutes TTL
        redisService.save("otp:" + receivedEmail, otpCode, 5, TimeUnit.MINUTES);

        return true;
    }

    @Override
    public VerifiedOtpResponseDto verifyOtpForgotPassword(String otpCode) {
        // Since we key by email, we can't easily find by OTP code unless we iterate
        // (bad) or the request includes email.
        // BUT the interface only takes otpCode.
        // IF the previous system assumed unique OTPs across the system, that's weak.
        // However, looking at the previous implementation
        // `otpForgotPasswordRepository.findByOtpCode(otpCode)`, it seems it relied on
        // OTP uniqueness.
        // To support this without changing the API signature (which I should try to
        // avoid if possible, but might need to),
        // I would need a mapping otp -> email.
        // Let's assume for now I should change the flow or look up.
        // Actually, searching keys "otp:*" is expensive.
        // PROPOSAL: Change API to require Email + OTP, or store "otp_code:" + otpCode
        // -> email in Redis as well.
        // Let's store "otp_mapping:" + otpCode -> email.

        String email = redisService.get("otp_mapping:" + otpCode);
        if (email == null) {
            throw new NotFoundException(ErrorMessage.OtpForgotPassword.ERR_NOT_FOUND, new String[] { otpCode });
        }

        // Check if the main OTP key still exists (it should if mapping exists, but good
        // to double check or just rely on mapping TTL)
        String storedOtp = redisService.get("otp:" + email);
        if (storedOtp == null || !storedOtp.equals(otpCode)) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_OTP_EXPIRED, new String[] { otpCode });
        }

        String resetPasswordToken = UUID.randomUUID().toString() + System.currentTimeMillis();

        // Save reset token to Redis: "reset_token:" + token -> email
        redisService.save("reset_token:" + resetPasswordToken, email, 5, TimeUnit.MINUTES);

        // Clean up OTP
        redisService.delete("otp:" + email);
        redisService.delete("otp_mapping:" + otpCode);

        VerifiedOtpResponseDto response = VerifiedOtpResponseDto.builder()
                .resetPasswordToken(resetPasswordToken)
                .resetPasswordExpiryDate(LocalDateTime.now().plusMinutes(5)) // Approximate
                .build();
        log.info("verified successfully otp code: {}", otpCode);
        return response;
    }

    @Override
    public boolean confirmChangeNewPassword(ConfirmNewPasswordRequestDto requestDto) {
        log.info("Confirming new password");
        String email = redisService.get("reset_token:" + requestDto.getResetPasswordToken());

        if (email == null) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_CHANGE_PASSWORD_EXPIRED);
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmNewPassword())) {
            throw new BadRequestException(ErrorMessage.OtpForgotPassword.ERR_PASSWORD_NOT_MATCHED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_EMAIL, new String[] { email }));

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);

        // Clean up reset token
        redisService.delete("reset_token:" + requestDto.getResetPasswordToken());

        log.info("Reset new password successfully");
        return true;
    }

    private String generateOtpCode(String receivedEmail) {
        // Generate 6-digit OTP (100000 - 999999)
        int otpCodeInt = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(otpCodeInt);

        // Store mapping for findByOtpCode support
        redisService.save("otp_mapping:" + otpCode, receivedEmail, 5, TimeUnit.MINUTES);

        return otpCode;
    }

}
