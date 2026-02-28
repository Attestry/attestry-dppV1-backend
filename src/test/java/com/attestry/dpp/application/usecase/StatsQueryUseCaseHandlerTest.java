package com.attestry.dpp.application.usecase;

import com.attestry.dpp.application.dto.result.TodayStatsResult;
import com.attestry.dpp.application.usecase.query.StatsQueryUseCaseHandler;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.repository.LedgerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsQueryUseCaseHandlerTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @InjectMocks
    private StatsQueryUseCaseHandler handler;

    @Test
    @DisplayName("getTodayStats: 당일 지표를 집계해 반환한다")
    void getTodayStats_returnsAggregatedCounts() {
        when(ledgerRepository.countByEventActionAndOccurredAtBetween(
                eq(LedgerAction.MINTED), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(3L);
        when(ledgerRepository.countByEventActionAndOccurredAtBetween(
                eq(LedgerAction.TRANSFER_COMPLETED), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(2L);
        when(ledgerRepository.countByOccurredAtBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(10L);

        TodayStatsResult result = handler.getTodayStats();

        assertThat(result.getAssets()).isEqualTo(3L);
        assertThat(result.getTransfers()).isEqualTo(2L);
        assertThat(result.getLedger()).isEqualTo(10L);
    }
}
