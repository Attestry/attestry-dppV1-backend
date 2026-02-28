package com.attestry.dpp.application.usecase.command;

import com.attestry.dpp.application.dto.request.RegistrationSubmitRequest;
import com.attestry.dpp.application.dto.result.RegistrationRequestResult;

public interface RegistrationCommandUseCase {
    RegistrationRequestResult submitRequest(RegistrationSubmitRequest request, String requesterId);
    void approveRequest(String requestId, String adminId);
    void rejectRequest(String requestId);
}
