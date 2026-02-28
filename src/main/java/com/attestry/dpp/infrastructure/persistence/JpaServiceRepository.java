package com.attestry.dpp.infrastructure.persistence;

import com.attestry.dpp.domain.model.ServiceCase;
import com.attestry.dpp.domain.repository.ServiceRepository;

import com.attestry.dpp.domain.model.ServiceCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaServiceRepository extends JpaRepository<ServiceCase, String>, ServiceRepository {
}
