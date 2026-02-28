package com.attestry.dpp.domain.repository;

import com.attestry.dpp.domain.model.Ownership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface OwnershipRepository {
    Optional<Ownership> findById(String passportId);

    List<Ownership> findByOwnerId(String ownerId);
    
    Page<Ownership> findByOwnerId(String ownerId, Pageable pageable);

    Ownership save(Ownership ownership);
}
