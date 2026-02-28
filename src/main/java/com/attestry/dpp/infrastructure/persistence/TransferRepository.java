package com.attestry.dpp.infrastructure.persistence;

import com.attestry.dpp.domain.model.TransferToken;
import com.attestry.dpp.domain.model.TransferState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<TransferToken, String> {

    Optional<TransferToken> findByCodeAndState(String code, TransferState state);

    /**
     * ID 또는 6자리 코드로 INITIATED 상태의 토큰을 단일 쿼리로 조회합니다.
     * 기존 findById → findByCode 이중 쿼리 방식을 단일 쿼리로 대체합니다.
     */
    @Query("SELECT t FROM TransferToken t WHERE t.id = :tokenOrCode OR (t.code = :tokenOrCode AND t.state = :state)")
    Optional<TransferToken> findByIdOrCodeAndState(
            @Param("tokenOrCode") String tokenOrCode,
            @Param("state") TransferState state);
}
