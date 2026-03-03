package com.attestry.dpp.infrastructure.persistence;

import com.attestry.dpp.domain.model.RegistrationRequest;
import com.attestry.dpp.domain.model.RegistrationStatus;
import com.attestry.dpp.domain.repository.RegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JpaRegistrationRepository extends JpaRepository<RegistrationRequest, String>, RegistrationRepository {
    Page<RegistrationRequest> findByStatus(RegistrationStatus status, Pageable pageable);

    Page<RegistrationRequest> findByRequesterId(String requesterId, Pageable pageable);

    List<RegistrationRequest> findBySerialNumberAndModelName(String serialNumber, String modelName);
}
