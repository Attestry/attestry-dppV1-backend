package com.attestry.dpp.infrastructure.web.mapper;

import com.attestry.dpp.application.dto.result.RegistrationRequestResult;
import com.attestry.dpp.infrastructure.web.response.RegistrationRequestResponse;

public final class RegistrationResponseMapper {

    private RegistrationResponseMapper() {
    }

    public static RegistrationRequestResponse toRegistrationRequestResponse(RegistrationRequestResult result) {
        return RegistrationRequestResponse.of(
                result.getRequestId(),
                result.getModelName(),
                result.getSerialNumber(),
                result.getEvidenceUrls(),
                result.getRequesterId(),
                result.getStatus(),
                result.getCreatedAt());
    }
}
