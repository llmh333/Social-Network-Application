package com.example.projectbase.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.projectbase.domain.dto.response.AwsS3ResponseDto;
import com.example.projectbase.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class AwsS3ServiceImpl implements AwsS3Service {

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    private final AmazonS3 awsS3Client;

    @Override
    public String uploadFile(File file) {
        String sanitizedName = sanitizeFileName(file.getName());
        String uniqueFileName = System.currentTimeMillis() + "_" + sanitizedName;
        log.info("Uploading file '{}' as '{}' to AWS S3 bucket '{}'", file.getName(), uniqueFileName, bucketName);
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.length());

            PutObjectRequest request = new PutObjectRequest(bucketName, uniqueFileName, file);
            request.setMetadata(objectMetadata);

            awsS3Client.putObject(request);

            log.info("Successfully uploaded file to AWS S3");
            return awsS3Client.getUrl(bucketName, uniqueFileName).toString();
        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
        }
    }

    @Override
    public AwsS3ResponseDto uploadMultiFile (List<File> files) {
        AwsS3ResponseDto awsS3ResponseDto = new AwsS3ResponseDto();
        List<String> urls = files.stream()
                .map(this::uploadFile)
                .collect(Collectors.toList());
        awsS3ResponseDto.setUrls(urls);
        return awsS3ResponseDto;
    }

    /**
     * Tải nhiều file từ S3 về và lưu thành các file tạm trên máy chủ.
     * @param s3Urls Danh sách các URL của S3.
     * @return Danh sách các đối tượng File đã được tải về.
     */
    @Override
    public List<File> downloadMultipleFiles(List<String> s3Urls) {
        return s3Urls.stream()
                .map(this::downloadFileFromUrl)
                .collect(Collectors.toList());
    }

    /**
     * Tải một file từ URL S3 về thành một file tạm.
     * @param s3Url URL của đối tượng trên S3.
     * @return Đối tượng File tạm.
     */
    private File downloadFileFromUrl(String s3Url) {
        try {
            URL url = new URL(s3Url);
            String key = url.getPath().substring(1);
            log.info("Downloading object with key '{}' from bucket '{}'", key, bucketName);

            // 2. Lấy đối tượng từ S3
            S3Object s3Object = awsS3Client.getObject(new GetObjectRequest(bucketName, key));
            InputStream inputStream = s3Object.getObjectContent();

            File tempFile = createTempFileFromKey(key);
            log.info("Created temporary file: {}", tempFile.getAbsolutePath());

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] read_buf = new byte[1024];
                int read_len;
                while ((read_len = inputStream.read(read_buf)) > 0) {
                    fos.write(read_buf, 0, read_len);
                }
            }

            inputStream.close();
            log.info("Successfully downloaded file to {}", tempFile.getAbsolutePath());
            return tempFile;

        } catch (IOException e) {
            log.error("Error downloading file from S3 URL: {}", s3Url, e);
            throw new RuntimeException("Error downloading file: " + e.getMessage());
        }
    }

    /**
     * Helper để tạo file tạm với tên và phần mở rộng hợp lý.
     */
    private File createTempFileFromKey(String key) throws IOException {
        String prefix = "s3_download_";
        String suffix = "";

        if (key.contains(".")) {
            prefix = key.substring(0, key.lastIndexOf('.'));
            suffix = key.substring(key.lastIndexOf('.'));
        } else {
            prefix = key;
        }

        return File.createTempFile(prefix + "_", suffix);
    }

    private String sanitizeFileName(String fileName) {
        String sanitized = fileName.replaceAll("\\s+", "_");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9_.-]", "");
        return sanitized;
    }

    /**
     * Xóa nhiều đối tượng trên S3 dựa vào danh sách URL.
     * @param s3Urls Danh sách các URL của S3 cần xóa.
     */
    @Override
    public void deleteMultipleFiles(List<String> s3Urls) {
        if (s3Urls == null || s3Urls.isEmpty()) {
            log.warn("No S3 URLs provided for deletion.");
            return;
        }

        List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
        for (String urlStr : s3Urls) {
            try {
                URL url = new URL(urlStr);
                String key = url.getPath().substring(1);
                keys.add(new DeleteObjectsRequest.KeyVersion(key));
            } catch (Exception e) {
                log.error("Invalid S3 URL format, cannot extract key: {}", urlStr, e);
            }
        }

        if (keys.isEmpty()) {
            log.warn("No valid keys found to delete from S3.");
            return;
        }

        DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(bucketName)
                .withKeys(keys)
                .withQuiet(false);

        try {
            DeleteObjectsResult delObjRes = awsS3Client.deleteObjects(multiObjectDeleteRequest);
            log.info("Successfully deleted {} objects from S3 bucket '{}'.", delObjRes.getDeletedObjects().size(), bucketName);
            delObjRes.getDeletedObjects().forEach(deletedObject ->
                    log.debug("Deleted key: {}", deletedObject.getKey())
            );
        } catch (Exception e) {
            log.error("Error deleting objects from S3", e);
        }
    }
}
