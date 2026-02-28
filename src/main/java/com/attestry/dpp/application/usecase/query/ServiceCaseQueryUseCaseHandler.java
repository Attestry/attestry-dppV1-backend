package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.ServiceCaseResult;
import com.attestry.dpp.domain.exception.NotFoundException;
import com.attestry.dpp.domain.model.ServiceCase;
import com.attestry.dpp.domain.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceCaseQueryUseCaseHandler implements ServiceCaseQueryUseCase {

    private final ServiceRepository serviceRepository;

    /**
     * 서비스 케이스 단건 상세를 조회합니다.
     */
    @Transactional(readOnly = true)
    public ServiceCaseResult getServiceCase(String caseId) {
        ServiceCase serviceCase = serviceRepository.findById(caseId)
                .orElseThrow(() -> new NotFoundException("서비스 케이스를 찾을 수 없습니다: " + caseId));
        return toResult(serviceCase);
    }

    private ServiceCaseResult toResult(ServiceCase serviceCase) {
        return ServiceCaseResult.of(
                serviceCase.getId(),
                serviceCase.getAsset().getId(),
                serviceCase.getProvider().getId(),
                serviceCase.getApprovedBy() != null ? serviceCase.getApprovedBy().getId() : null,
                serviceCase.getKind().name(),
                serviceCase.getState().name(),
                serviceCase.getSubmittedAt(),
                serviceCase.getApprovedAt());
    }
}
