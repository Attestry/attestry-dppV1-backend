package com.attestry.dpp.infrastructure.persistence;

import com.attestry.dpp.domain.model.Ownership;
import com.attestry.dpp.domain.repository.OwnershipRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface JpaOwnershipRepository extends JpaRepository<Ownership, String>, OwnershipRepository {
    Page<Ownership> findByOwnerId(String ownerId, Pageable pageable);
}
