package com.attestry.dpp.domain.repository;

import com.attestry.dpp.domain.model.Asset;
import java.util.Optional;

public interface AssetRepository {
    Optional<Asset> findById(String id);

    Asset save(Asset asset);

    long count();
}
