package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.TodayStatsResult;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class StatsQueryUseCaseHandler implements StatsQueryUseCase {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");
    private static final ZoneId STORAGE_ZONE = ZoneOffset.UTC;

    private final LedgerRepository ledgerRepository;

    /**
     * 당일 기준 핵심 지표(민팅/이전/원장 이벤트)를 집계합니다.
     */
    @Transactional(readOnly = true)
    public TodayStatsResult getTodayStats() {
        ZonedDateTime kstStart = LocalDate.now(BUSINESS_ZONE).atStartOfDay(BUSINESS_ZONE);
        LocalDateTime startOfDay = kstStart.withZoneSameInstant(STORAGE_ZONE).toLocalDateTime();
        LocalDateTime endOfDay = kstStart.plusDays(1).withZoneSameInstant(STORAGE_ZONE).toLocalDateTime();

        long assets = ledgerRepository.countByEventActionAndOccurredAtBetween(LedgerAction.MINTED, startOfDay, endOfDay);
        long transfers = ledgerRepository.countByEventActionAndOccurredAtBetween(
                LedgerAction.TRANSFER_COMPLETED, startOfDay, endOfDay);
        long ledger = ledgerRepository.countByOccurredAtBetween(startOfDay, endOfDay);

        return TodayStatsResult.of(assets, transfers, ledger);
    }
}
