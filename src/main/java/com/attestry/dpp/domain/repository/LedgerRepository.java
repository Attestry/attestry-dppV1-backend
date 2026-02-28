package com.attestry.dpp.domain.repository;

import com.attestry.dpp.domain.model.LedgerAction;
import com.attestry.dpp.domain.model.LedgerEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LedgerRepository {
    List<LedgerEntry> findByPassportIdOrderBySeqAsc(String passportId);

    Optional<LedgerEntry> findFirstByPassportIdOrderBySeqDesc(String passportId);

    long countByEventActionAndOccurredAtBetween(LedgerAction eventAction, LocalDateTime start, LocalDateTime end);

    long countByOccurredAtBetween(LocalDateTime start, LocalDateTime end);

    LedgerEntry save(LedgerEntry entry);

    void flush();
}
