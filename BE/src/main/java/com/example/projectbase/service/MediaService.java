package com.example.projectbase.service;

import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

public interface MediaService {

    public PaginationResponseDto<MediaResponseDto> getAllMedia(PaginationFullRequestDto paginationFullRequestDto);
    public MediaResponseDto uploadVideo(MultipartFile multipartFile, File file);

    public MediaResponseDto uploadImage(MultipartFile file);
    public List<MediaResponseDto> uploadMultiImage(List<MultipartFile> file);

    public boolean deleteMedia(List<String> publicIdList);
}
