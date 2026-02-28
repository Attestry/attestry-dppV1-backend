package com.attestry.dpp.application.dto.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(staticName = "of")
public class RegistrationRequestResult {
    private final String requestId;
    private final String modelName;
    private final String serialNumber;
    private final String evidenceUrls;
    private final String requesterId;
    private final String status;
    private final LocalDateTime createdAt;
}
