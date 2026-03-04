package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.RegistrationRequestResult;
import com.attestry.dpp.application.port.FileReadUrlPort;
import com.attestry.dpp.application.usecase.query.RegistrationQueryUseCase;
import com.attestry.dpp.domain.model.RegistrationRequest;
import com.attestry.dpp.domain.model.RegistrationStatus;
import com.attestry.dpp.domain.repository.RegistrationRepository;
import com.attestry.dpp.domain.util.EvidenceUrlParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationQueryUseCaseHandler implements RegistrationQueryUseCase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RegistrationRepository registrationRepository;
    private final FileReadUrlPort fileReadUrlPort;

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
    public Page<RegistrationRequestResult> listRequestsByRequester(String requesterId, Pageable pageable) {
        return registrationRepository.findByRequesterId(requesterId, pageable)
                .map(this::toResponse);
    }

    private RegistrationRequestResult toResponse(RegistrationRequest entity) {
        return RegistrationRequestResult.of(
                entity.getRequestId(),
                entity.getModelName(),
                entity.getSerialNumber(),
                toDisplayEvidenceUrls(entity.getEvidenceUrls()),
                entity.getRequesterId(),
                entity.getStatus().name(),
                entity.getCreatedAt());
    }

    private String toDisplayEvidenceUrls(String rawEvidenceUrls) {
        if (rawEvidenceUrls == null || rawEvidenceUrls.isBlank()) {
            return rawEvidenceUrls;
        }
        List<String> parsed = EvidenceUrlParser.parse(rawEvidenceUrls);
        if (parsed.isEmpty()) {
            return rawEvidenceUrls;
        }

        List<String> signed = parsed.stream()
                .map(fileReadUrlPort::createPresignedReadUrl)
                .toList();

        if (rawEvidenceUrls.trim().startsWith("[")) {
            try {
                return OBJECT_MAPPER.writeValueAsString(signed);
            } catch (Exception ignored) {
                return rawEvidenceUrls;
            }
        }
        return signed.get(0);
    }
}
