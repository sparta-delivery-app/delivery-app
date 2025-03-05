package com.example.deliveryapp.client;

import com.example.deliveryapp.domain.common.exception.CustomException;
import com.example.deliveryapp.domain.common.exception.ErrorCode;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${s3.bucket}")
    private String bucket;

    private final S3Operations s3Operations;

    public String uploadImage(MultipartFile file, String folder) {
        validateImage(file);

        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String fullName = folder + "/" + fileName;
        ObjectMetadata metadata = ObjectMetadata.builder()
                .contentType(file.getContentType())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Operations.upload(bucket, fullName, inputStream, metadata);
        } catch (IOException e) {
            log.error("[파일 업로드 실패] ", e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return fileName;
    }

    public void deleteImage(String folder, String fileName) {
        String fullName = folder + "/" + fileName;
        s3Operations.deleteObject(bucket, fullName);
    }

    private static void validateImage(MultipartFile image) {
        if (image.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
        }
    }
}
