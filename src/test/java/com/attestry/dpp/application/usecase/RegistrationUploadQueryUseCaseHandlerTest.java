package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.port.FileUploadUrlPort;
import com.attestry.dpp.application.usecase.query.RegistrationUploadQueryUseCaseHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationUploadQueryUseCaseHandlerTest {

    @Mock
    private FileUploadUrlPort fileUploadUrlPort;

    @InjectMocks
    private RegistrationUploadQueryUseCaseHandler handler;

    @Test
    @DisplayName("getUploadUrl: 파일명으로 presigned URL을 발급한다")
    void getUploadUrl_returnsPresignedUrl() {
        when(fileUploadUrlPort.createPresignedUploadUrl("evidence.jpg"))
                .thenReturn("https://minio/presigned-url");

        String result = handler.getUploadUrl("evidence.jpg");

        assertThat(result).isEqualTo("https://minio/presigned-url");
    }
}

