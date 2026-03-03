package com.attestry.dpp.domain.repository;

import com.attestry.dpp.domain.model.RegistrationRequest;
import com.attestry.dpp.domain.model.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository {
    Optional<RegistrationRequest> findById(String id);

    // 관리자용 페이징 조회 — 전체 조회 방지
    Page<RegistrationRequest> findByStatus(RegistrationStatus status, Pageable pageable);

    Page<RegistrationRequest> findByRequesterId(String requesterId, Pageable pageable);

    List<RegistrationRequest> findBySerialNumberAndModelName(String serialNumber, String modelName);

    RegistrationRequest save(RegistrationRequest request);
}
