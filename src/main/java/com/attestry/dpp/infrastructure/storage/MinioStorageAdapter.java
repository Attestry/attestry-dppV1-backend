package com.attestry.dpp.infrastructure.storage;

import com.attestry.dpp.application.port.FileUploadUrlPort;
import com.attestry.dpp.application.port.FileReadUrlPort;
import com.attestry.dpp.domain.exception.CustomException;
import com.attestry.dpp.domain.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioStorageAdapter implements FileUploadUrlPort, FileReadUrlPort {

    private MinioClient minioClient;
    private MinioClient presignMinioClient;

    @Value("${minio.bucket-name:dpp-evidence}")
    private String bucketName;

    @Value("${minio.url:http://localhost:9000}")
    private String url;

    @Value("${minio.public-url:}")
    private String publicUrl;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        String presignEndpoint = hasText(publicUrl) ? publicUrl : url;
        this.presignMinioClient = MinioClient.builder()
                .endpoint(presignEndpoint)
                .credentials(accessKey, secretKey)
                .build();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO 버킷 생성 완료: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("MinIO 버킷 초기화 실패 (버킷이 이미 존재하거나 연결 문제): {}", e.getMessage());
        }
    }

    /**
     * 클라이언트 직접 업로드용 사전서명 URL을 생성합니다.
     * 저장 경로는 UUID 접두어를 붙여 파일명 충돌을 방지합니다.
     */
    @Override
    public String createPresignedUploadUrl(String originalFilename) {
        try {
            String objectName = UUID.randomUUID() + "_" + originalFilename;
            return presignMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            // 외부 스토리지 예외는 도메인 공통 예외 포맷으로 변환
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "Error generating pre-signed URL: " + e.getMessage());
        }
    }

    @Override
    public String createPresignedReadUrl(String rawUrlOrObjectPath) {
        if (!hasText(rawUrlOrObjectPath)) {
            return rawUrlOrObjectPath;
        }
        String objectName = extractObjectName(rawUrlOrObjectPath);
        if (!hasText(objectName)) {
            return rawUrlOrObjectPath;
        }

        try {
            return presignMinioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.warn("증빙 이미지 조회 URL 생성 실패. 원본 URL 반환: {}", e.getMessage());
            return rawUrlOrObjectPath;
        }
    }

    private String extractObjectName(String rawUrlOrObjectPath) {
        String value = rawUrlOrObjectPath.trim();
        try {
            URI uri = URI.create(value);
            String path = uri.getPath();
            if (!hasText(path)) {
                return value;
            }
            String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
            String bucketPrefix = bucketName + "/";
            if (normalizedPath.startsWith(bucketPrefix)) {
                return normalizedPath.substring(bucketPrefix.length());
            }
            return normalizedPath;
        } catch (Exception ignored) {
            return value;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
