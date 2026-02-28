package com.attestry.dpp.domain.repository;

import com.attestry.dpp.domain.model.ServiceCase;
import java.util.List;
import java.util.Optional;

public interface ServiceRepository {
    Optional<ServiceCase> findById(String id);

    List<ServiceCase> findByProviderId(String providerId);

    List<ServiceCase> findByAssetId(String assetId);

    ServiceCase save(ServiceCase serviceCase);
}
