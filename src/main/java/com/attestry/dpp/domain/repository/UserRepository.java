package com.attestry.dpp.domain.repository;

import com.attestry.dpp.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    Page<User> findByStatus(User.Status status, Pageable pageable);

    User save(User user);
}
