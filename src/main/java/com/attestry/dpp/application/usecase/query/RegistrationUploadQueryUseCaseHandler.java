package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.port.FileUploadUrlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationUploadQueryUseCaseHandler implements RegistrationUploadQueryUseCase {

    private final FileUploadUrlPort fileUploadUrlPort;

    /**
     * 클라이언트 직접 업로드를 위한 presigned URL을 발급합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public String getUploadUrl(String filename) {
        return fileUploadUrlPort.createPresignedUploadUrl(filename);
    }
}
