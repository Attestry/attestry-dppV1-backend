package com.attestry.dpp.infrastructure.persistence;

import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.repository.PassportRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaPassportRepository extends JpaRepository<DigitalPassport, String>, PassportRepository {
    Optional<DigitalPassport> findByQrPublicCode(String qrPublicCode);

    Optional<DigitalPassport> findByQrPublicCodeIgnoreCase(String qrPublicCode);

    Optional<DigitalPassport> findByAssetId(String assetId);
}
