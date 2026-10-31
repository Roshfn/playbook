package com.playbook.auth.infrastructure.persistance.repository;

import com.playbook.auth.infrastructure.persistance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByPhone(String phone);

    @Modifying
    @Query(value = "UPDATE users SET last_login_at = :at WHERE id = :id", nativeQuery = true)
    void updateLastLogin(@Param("id") UUID id, @Param("at") Instant at);
}