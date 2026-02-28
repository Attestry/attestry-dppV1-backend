package com.attestry.dpp.infrastructure.web.mapper;

import com.attestry.dpp.application.dto.result.ServiceCaseResult;
import com.attestry.dpp.application.dto.result.ServiceSubmitResult;
import com.attestry.dpp.infrastructure.web.response.ServiceCaseResponse;
import com.attestry.dpp.infrastructure.web.response.ServiceSubmitResponse;

public final class ServiceResponseMapper {

    private ServiceResponseMapper() {
    }

    public static ServiceSubmitResponse toServiceSubmitResponse(ServiceSubmitResult result) {
        return ServiceSubmitResponse.from(result.getCaseId());
    }

    public static ServiceCaseResponse toServiceCaseResponse(ServiceCaseResult result) {
        return ServiceCaseResponse.of(
                result.getCaseId(),
                result.getAssetId(),
                result.getProviderId(),
                result.getApprovedById(),
                result.getKind(),
                result.getState(),
                result.getSubmittedAt(),
                result.getApprovedAt());
    }
}
