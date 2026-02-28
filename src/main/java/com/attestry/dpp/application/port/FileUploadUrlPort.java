package com.attestry.dpp.application.port;

public interface FileUploadUrlPort {
    String createPresignedUploadUrl(String originalFilename);
}
