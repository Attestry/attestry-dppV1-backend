package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.TodayStatsResult;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatsQueryUseCaseHandler implements StatsQueryUseCase {

    private final LedgerRepository ledgerRepository;

    /**
     * 당일 기준 핵심 지표(민팅/이전/원장 이벤트)를 집계합니다.
     */
    @Transactional(readOnly = true)
    public TodayStatsResult getTodayStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long assets = ledgerRepository.countByEventActionAndOccurredAtBetween(LedgerAction.MINTED, startOfDay, endOfDay);
        long transfers = ledgerRepository.countByEventActionAndOccurredAtBetween(
                LedgerAction.TRANSFER_COMPLETED, startOfDay, endOfDay);
        long ledger = ledgerRepository.countByOccurredAtBetween(startOfDay, endOfDay);

        return TodayStatsResult.of(assets, transfers, ledger);
    }
}
