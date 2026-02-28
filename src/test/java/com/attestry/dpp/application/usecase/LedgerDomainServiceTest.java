package com.attestry.dpp.application.usecase;
import com.attestry.dpp.domain.service.LedgerService;


import com.attestry.dpp.domain.model.Asset;
import com.attestry.dpp.domain.model.AssetStatus;
import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.LedgerEntry;
import com.attestry.dpp.domain.repository.LedgerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerDomainServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;

    @InjectMocks
    private LedgerService ledgerService;

    private DigitalPassport passport;

    @BeforeEach
    void setUp() {
        Asset asset = Asset.builder().id("A1").modelName("Model X").serialNumber("SN1").status(AssetStatus.ACTIVE).build();
        passport = DigitalPassport.builder().id("P1").asset(asset).qrPublicCode("QR1").build();
    }

    @Test
    @DisplayName("첫 번째 원장 항목은 seq=1, prevHash=null로 생성된다")
    void firstEntryHasSequenceOneAndNullPrevHash() {
        when(ledgerRepository.findFirstByPassportIdOrderBySeqDesc("P1")).thenReturn(Optional.empty());
        when(ledgerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LedgerEntry entry = ledgerService.recordEntry(passport, LedgerAction.MINTED, "BRAND", "admin-1", null, null);

        assertThat(entry.getSeq()).isEqualTo(1);
        assertThat(entry.getPrevHash()).isNull();
        assertThat(entry.getHash()).isNotNull().hasSize(64);
        assertThat(entry.getEventAction()).isEqualTo(LedgerAction.MINTED);
    }

    @Test
    @DisplayName("두 번째 원장 항목은 이전 항목의 hash를 prevHash로 참조한다")
    void secondEntryReferencesPreviousHash() {
        LedgerEntry prev = LedgerEntry.builder().id("LE1").seq(1).hash("abc123prevhash").build();

        when(ledgerRepository.findFirstByPassportIdOrderBySeqDesc("P1")).thenReturn(Optional.of(prev));
        when(ledgerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LedgerEntry entry = ledgerService.recordEntry(passport, LedgerAction.CLAIMED, "OWNER", "user-1", null, null);

        assertThat(entry.getSeq()).isEqualTo(2);
        assertThat(entry.getPrevHash()).isEqualTo("abc123prevhash");
        assertThat(entry.getHash()).isNotNull().isNotEqualTo("abc123prevhash");
    }

    @Test
    @DisplayName("원장 항목 생성 시 ledgerRepository.save와 flush가 호출된다")
    void recordEntrySavesAndFlushes() {
        when(ledgerRepository.findFirstByPassportIdOrderBySeqDesc("P1")).thenReturn(Optional.empty());
        when(ledgerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ledgerService.recordEntry(passport, LedgerAction.MINTED, "BRAND", "admin-1", "{}", "corr-1");

        verify(ledgerRepository).save(any(LedgerEntry.class));
        verify(ledgerRepository).flush();
    }
}
