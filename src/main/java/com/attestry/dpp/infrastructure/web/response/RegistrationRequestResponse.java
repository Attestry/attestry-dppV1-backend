package com.attestry.dpp.infrastructure.web.response;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
public class RegistrationRequestResponse {
    private final String requestId;
    private final String modelName;
    private final String serialNumber;
    private final String evidenceUrls;
    private final String requesterId;
    private final String status;
    private final LocalDateTime createdAt;
}
