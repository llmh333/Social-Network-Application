package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.SuccessMessage;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.request.ConfirmNewPasswordRequestDto;
import com.example.projectbase.domain.dto.response.VerifiedOtpResponseDto;
import com.example.projectbase.service.OtpForgotPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@RestApiV1
@RequiredArgsConstructor
@Log4j2
public class OtpForgotPasswordController {

    private final OtpForgotPasswordService otpForgotPasswordService;
    private final MessageSource messageSource;

    @PostMapping(value = UrlConstant.OtpCode.SEND_OTP)
    public ResponseEntity<?> sendOtp(@RequestParam("email") String receivedEmail) {
        boolean result = otpForgotPasswordService.sendOtpForgotPassword(receivedEmail);
        if (!result) {
            String message = messageSource.getMessage(ErrorMessage.OtpForgotPassword.ERR_SEND_FAILED,null, LocaleContextHolder.getLocale());
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, message);
        }
        String message = messageSource.getMessage(SuccessMessage.ForgotPassword.SEND_OTP_SUCCESSFULLY ,null, LocaleContextHolder.getLocale());
        return VsResponseUtil.successWithMessage(message);
    }

    @PostMapping(value = UrlConstant.OtpCode.VERIFY_OTP)
    public ResponseEntity<?> verifyOtp(@RequestParam("otpCode") String otpCode) {
        VerifiedOtpResponseDto response = otpForgotPasswordService.verifyOtpForgotPassword(otpCode);
        if (response == null) {
            String message = messageSource.getMessage(ErrorMessage.OtpForgotPassword.ERR_VERIFY_FAILED,null, LocaleContextHolder.getLocale());
            return VsResponseUtil.error(HttpStatus.BAD_REQUEST, message);
        }
        return VsResponseUtil.success(response);
    }

    @PostMapping(value = UrlConstant.OtpCode.CHANGE_PASSWORD)
    public ResponseEntity<?> changePassword(@RequestBody @Valid ConfirmNewPasswordRequestDto requestDto) {
        otpForgotPasswordService.confirmChangeNewPassword(requestDto);
        String message = messageSource.getMessage(SuccessMessage.ForgotPassword.RESET_PASSWORD_SUCCESSFULLY, null, LocaleContextHolder.getLocale());
        return VsResponseUtil.successWithMessage(message);
    }
}
