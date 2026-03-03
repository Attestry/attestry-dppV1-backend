package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.RegistrationRequestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RegistrationQueryUseCase {
    Page<RegistrationRequestResult> listAllRequests(Pageable pageable);
    Page<RegistrationRequestResult> listRequestsByRequester(String requesterId, Pageable pageable);
}
