package com.example.projectbase.service.impl;

import com.example.projectbase.exception.BadRequestException;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class VideoProcessingService {

    @Async
    public File compressVideo(MultipartFile multipartFile) throws IOException, InterruptedException {
        File originalFile = File.createTempFile("original_", ".mp4");
        File compressedFile = File.createTempFile("compressed_", ".mp4");

        try {
            multipartFile.transferTo(originalFile);
            if (compressedFile.exists()) {
                compressedFile.delete();
            }
            if (compressedFile.exists()) {
                compressedFile.delete();
            }
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", originalFile.getAbsolutePath(),
                    "-r", "30",
                    "-c:v", "libx264",
                    "-crf", "23",
                    "-preset", "fast",
                    "-tune", "fastdecode",
                    "-vf", "scale=w=720:h=1080:force_original_aspect_ratio=decrease,pad=720:1080:(ow-iw)/2:(oh-ih)/2",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    compressedFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg] {}", line);
                }
            }
            boolean finished = process.waitFor(150, TimeUnit.SECONDS);
            if (!finished) {
                log.warn("[FFmpeg] Video process timed out. Destroying process...");
                process.destroyForcibly();
                throw new BadRequestException("Video process timed out.");
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new BadRequestException("FFmpeg failed with exit code " + exitCode);
            }
            return compressedFile;
        } finally {
            if (originalFile.exists()) {
                originalFile.delete();
            }
        }

    }

    @Async
    public File compressAudio(MultipartFile multipartFile) throws IOException, InterruptedException {
        File originalFile = File.createTempFile("original_", ".mp3");
        File compressedFile = File.createTempFile("compressed_", ".m4a");

        try {
            multipartFile.transferTo(originalFile);
            if (compressedFile.exists()) {
                compressedFile.delete();
            }
            if (compressedFile.exists()) {
                compressedFile.delete();
            }
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", originalFile.getAbsolutePath(),
                    "-vn",
                    "-c:a", "aac",
                    "-b:a", "192k",
                    compressedFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg] {}", line);
                }
            }
            boolean finished = process.waitFor(150, TimeUnit.SECONDS);
            if (!finished) {
                log.warn("[FFmpeg] Video process timed out. Destroying process...");
                process.destroyForcibly();
                throw new BadRequestException("Video process timed out.");
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new BadRequestException("FFmpeg failed with exit code " + exitCode);
            }
            return compressedFile;
        } finally {
            if (originalFile.exists()) {
                originalFile.delete();
            }
        }
    }

}
