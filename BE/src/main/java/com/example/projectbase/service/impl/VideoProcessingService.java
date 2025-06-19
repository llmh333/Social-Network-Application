package com.example.projectbase.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletableFuture;

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
                "-r", "30",
                "-c:v", "libx264",
                "-crf", "28",
                "-preset", "superfast",
                "-tune", "fastdecode",
                "-vf", "scale='min(1080,iw)':'min(1920,ih)':force_original_aspect_ratio=decrease",
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "faststart",
                compressedFile.getAbsolutePath()
        );
        Process p = pb.inheritIO().start();
        p.waitFor();
        return compressedFile;
    }

    @Async
    public File compressAudio(MultipartFile multipartFile) throws IOException, InterruptedException {
        File originalFile = File.createTempFile("original_", ".mp3");
        multipartFile.transferTo(originalFile);
        File compressedFile = File.createTempFile("compressed_", ".mp3");
        if (compressedFile.exists()) {
            compressedFile.delete();
        }
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", originalFile.getAbsolutePath(),
                "-vn",
                "-ac", "2",
                "-b:a", "256k",
                "-preset", "ultrafast",
                compressedFile.getAbsolutePath()
        );
        Process p = pb.inheritIO().start();
        p.waitFor();
        return compressedFile;
    }

}
