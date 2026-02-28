package com.attestry.dpp.infrastructure.persistence;

import com.attestry.dpp.domain.model.User;
import com.attestry.dpp.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, String>, UserRepository {
    Optional<User> findByEmail(String email);
}
