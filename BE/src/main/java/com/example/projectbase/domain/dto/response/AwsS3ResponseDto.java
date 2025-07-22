package com.example.projectbase.domain.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AwsS3ResponseDto {
    private String type;
    private List<String> urls;
}
