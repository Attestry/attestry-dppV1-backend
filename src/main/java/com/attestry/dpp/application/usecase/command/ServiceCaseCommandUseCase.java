package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.ServiceSubmitRequest;
import com.attestry.dpp.application.dto.result.ServiceSubmitResult;

public interface ServiceCaseCommandUseCase {
    ServiceSubmitResult submitService(ServiceSubmitRequest request, String providerId);
    void completeService(String caseId, String providerId);
    void approveService(String caseId, String ownerId);
}
