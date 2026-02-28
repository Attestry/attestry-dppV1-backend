package com.attestry.dpp.domain.repository;

import com.attestry.dpp.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    java.util.List<User> findByStatus(User.Status status);

    User save(User user);
}
