package com.attestry.dpp.domain.repository;

import com.attestry.dpp.domain.model.DigitalPassport;
import java.util.Optional;
import java.util.List;

public interface PassportRepository {
    Optional<DigitalPassport> findById(String id);

    Optional<DigitalPassport> findByQrPublicCode(String qrPublicCode);

    // 자산 ID로 직접 조회 — findAll() 전체 로드 방지
    Optional<DigitalPassport> findByAssetId(String assetId);

    List<DigitalPassport> findAll();

    DigitalPassport save(DigitalPassport passport);
}
