package com.example.projectbase.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class VideoProcessingService {

    @Async
    public File compressVideo(MultipartFile file) throws IOException, InterruptedException {
        File originalFile = File.createTempFile("original_", ".mp4");

        file.transferTo(originalFile);
        File compressedFile = File.createTempFile("compressed_", ".mp4");
        if (compressedFile.exists()) {
            compressedFile.delete();
        }
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", originalFile.getAbsolutePath(),
                "-c:v", "libx264",
                "-crf", "28",
                "-preset", "fast",
                "-vf", "scale=720:1280",
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "faststart",
                compressedFile.getAbsolutePath()
        );
        Process p = pb.inheritIO().start();
        p.waitFor();
        return compressedFile;
    }

}
