package com.example.projectbase.service.impl;

import com.example.projectbase.domain.dto.request.UserSettingRequestDto;
import com.example.projectbase.domain.entity.User;
import com.example.projectbase.domain.entity.UserSetting;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.repository.UserSettingRepository;
import com.example.projectbase.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl implements UserSettingService{
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;

    @Override
    public void updateUserSetting(String username, UserSettingRequestDto request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElse(new UserSetting());

        setting.setUser(user);
        setting.setTheme(request.getTheme());
        setting.setLanguage(request.getLanguage());

        userSettingRepository.save(setting);
    }
}
