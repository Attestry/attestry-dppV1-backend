package com.attestry.dpp.infrastructure.persistence;

import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.repository.AssetRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAssetRepository extends JpaRepository<Asset, String>, AssetRepository {
}
