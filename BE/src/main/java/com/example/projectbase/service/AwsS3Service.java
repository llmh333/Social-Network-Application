package com.example.projectbase.service;

import com.example.projectbase.domain.dto.response.AwsS3ResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface AwsS3Service {

    public String uploadFile(File file);

    public AwsS3ResponseDto uploadMultiFile (List<File> files);

    public void deleteMultipleFiles(List<String> s3Urls);

    public List<File> downloadMultipleFiles(List<String> s3Urls);
}
