package com.playbook.auth.domain.repository;

import com.playbook.auth.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    User save(User user);
    void updateLastLogin(UUID userId, Instant at);
}