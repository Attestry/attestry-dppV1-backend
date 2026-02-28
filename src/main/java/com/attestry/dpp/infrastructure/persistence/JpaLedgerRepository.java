package com.attestry.dpp.infrastructure.persistence;

import com.attestry.dpp.domain.model.LedgerEntry;
import com.attestry.dpp.domain.repository.LedgerRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLedgerRepository extends JpaRepository<LedgerEntry, String>, LedgerRepository {
}
