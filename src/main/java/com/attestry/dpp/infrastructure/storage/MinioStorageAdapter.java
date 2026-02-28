package com.attestry.dpp.infrastructure.storage;

import com.attestry.dpp.application.port.FileUploadUrlPort;
import com.attestry.dpp.domain.exception.CustomException;
import com.attestry.dpp.domain.exception.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class MinioStorageAdapter implements FileUploadUrlPort {

    private MinioClient minioClient;

    @Value("${minio.bucket-name:dpp-evidence}")
    private String bucketName;

    @Value("${minio.url:http://localhost:9000}")
    private String url;

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
    }

    /**
     * 클라이언트 직접 업로드용 사전서명 URL을 생성합니다.
     * 저장 경로는 UUID 접두어를 붙여 파일명 충돌을 방지합니다.
     */
    @Override
    public String createPresignedUploadUrl(String originalFilename) {
        try {
            String objectName = UUID.randomUUID() + "_" + originalFilename;
            return minioClient.getPresignedObjectUrl(
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
}
