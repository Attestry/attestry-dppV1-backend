package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.RegistrationRequestResult;
import com.attestry.dpp.application.usecase.query.RegistrationQueryUseCase;
import com.attestry.dpp.domain.model.RegistrationRequest;
import com.attestry.dpp.domain.model.RegistrationStatus;
import com.attestry.dpp.domain.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationQueryUseCaseHandler implements RegistrationQueryUseCase {

    private final RegistrationRepository registrationRepository;

    /**
     * 관리자용 대기(PENDING) 등록 요청 목록을 페이지로 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<RegistrationRequestResult> listAllRequests(Pageable pageable) {
        return registrationRepository.findByStatus(RegistrationStatus.PENDING, pageable)
                .map(this::toResponse);
    }

    /**
     * 요청자 본인이 제출한 등록 요청 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<RegistrationRequestResult> listRequestsByRequester(String requesterId) {
        return registrationRepository.findByRequesterId(requesterId).stream()
                .map(this::toResponse)
                .toList();
    }

    private RegistrationRequestResult toResponse(RegistrationRequest entity) {
        return RegistrationRequestResult.of(
                entity.getRequestId(),
                entity.getModelName(),
                entity.getSerialNumber(),
                entity.getEvidenceUrls(),
                entity.getRequesterId(),
                entity.getStatus().name(),
                entity.getCreatedAt());
    }
}
