package com.example.projectbase.domain.dto.request;

import com.example.projectbase.constant.LanguageSetting;
import com.example.projectbase.constant.ThemeSetting;
import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class UserSettingRequestDto {
    @NotNull
    private ThemeSetting theme;

    @NotNull
    private LanguageSetting language;
}
