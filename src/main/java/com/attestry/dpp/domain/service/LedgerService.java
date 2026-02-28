package com.attestry.dpp.domain.service;

import com.attestry.dpp.domain.model.DigitalPassport;
import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.LedgerEntry;
import com.attestry.dpp.domain.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 원장(Ledger) 해시 체인 관리 도메인 서비스.
 * 모든 원장 기록은 이 서비스를 통해 생성됩니다.
 */
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    @Transactional
    public LedgerEntry recordEntry(
            DigitalPassport passport,
            LedgerAction action,
            String actorRole,
            String actorId,
            String dataJson,
            String correlationId) {

        var lastEntryOpt = ledgerRepository.findFirstByPassportIdOrderBySeqDesc(passport.getId());
        int nextSeq = lastEntryOpt.map(entry -> entry.getSeq() + 1).orElse(1);
        String prevHash = lastEntryOpt.map(LedgerEntry::getHash).orElse(null);

        LedgerEntry entry = LedgerEntry.create(
                passport, nextSeq, action, actorRole, actorId,
                dataJson, correlationId, prevHash);

        LedgerEntry savedEntry = ledgerRepository.save(entry);
        ledgerRepository.flush();
        return savedEntry;
    }
}
