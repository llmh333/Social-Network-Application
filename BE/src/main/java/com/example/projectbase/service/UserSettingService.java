package com.example.projectbase.service;

import com.example.projectbase.domain.dto.request.UserSettingRequestDto;

public interface UserSettingService{
    public void updateUserSetting(String username, UserSettingRequestDto request);
}
