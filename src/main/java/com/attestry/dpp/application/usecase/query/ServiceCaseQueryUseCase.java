package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.ServiceCaseResult;

public interface ServiceCaseQueryUseCase {
    ServiceCaseResult getServiceCase(String caseId);
}
