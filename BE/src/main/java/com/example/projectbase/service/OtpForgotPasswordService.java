package com.example.projectbase.service;

import com.example.projectbase.domain.dto.request.ConfirmNewPasswordRequestDto;
import com.example.projectbase.domain.dto.response.VerifiedOtpResponseDto;

public interface OtpForgotPasswordService {

    public boolean sendOtpForgotPassword(String receivedEmail);
    public VerifiedOtpResponseDto verifyOtpForgotPassword(String otp);
    public boolean confirmChangeNewPassword(ConfirmNewPasswordRequestDto requestDto);
}
