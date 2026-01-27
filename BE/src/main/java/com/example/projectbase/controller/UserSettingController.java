package com.example.projectbase.controller;

import com.example.projectbase.base.RestApiV1;
import com.example.projectbase.base.VsResponseUtil;
import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.UrlConstant;
import com.example.projectbase.domain.dto.request.UserSettingRequestDto;
import com.example.projectbase.exception.UnauthorizedException;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestApiV1
@RequiredArgsConstructor
@Validated
public class UserSettingController {
    private final UserSettingService userSettingService;

    @PutMapping(UrlConstant.UserSetting.UPDATE_SETTING)
    public ResponseEntity<?> updateUserSetting(
            @RequestBody UserSettingRequestDto request) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        if (userPrincipal == null) {
            throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        }
        userSettingService.updateUserSetting(userPrincipal.getUsername(), request);
        return VsResponseUtil.success("User setting updated successfully");
    }
}
