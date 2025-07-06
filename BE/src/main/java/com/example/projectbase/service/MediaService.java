package com.example.projectbase.service;

import com.example.projectbase.domain.dto.pagination.PaginationFullRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationRequestDto;
import com.example.projectbase.domain.dto.pagination.PaginationResponseDto;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.security.UserPrincipal;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface MediaService {

    public PaginationResponseDto<MediaResponseDto> getAllMedia(PaginationFullRequestDto paginationFullRequestDto);
    public MediaResponseDto getMediaByPublicId(String publicId);
    public PaginationResponseDto<MediaResponseDto> getAudioByTitleOrCategoryOrSinger(PaginationRequestDto paginationRequestDto, String keyword);

    public MediaResponseDto uploadVideo(MultipartFile multipartFile) throws IOException, InterruptedException;
    public MediaResponseDto uploadImage(MultipartFile file) throws IOException, InterruptedException;
    public List<MediaResponseDto> uploadAudio(MultipartFile audioFile, MultipartFile thumbnailFile, String title, String category, String singerName) throws IOException, InterruptedException;
    public List<MediaResponseDto> uploadMultiImage(List<MultipartFile> file);

    public boolean deleteMedia(List<String> publicIdList);
}
