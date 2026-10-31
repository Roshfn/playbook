package com.playbook.auth.infrastructure.persistance.repository;

import com.playbook.auth.infrastructure.persistance.entity.UserSessionEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionJpaRepository extends JpaRepository<UserSessionEntity, UUID> {

    Optional<UserSessionEntity> findByRefreshTokenId(UUID refreshTokenId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update UserSessionEntity s set s.active = false " +
            "where s.userId = :userId and s.deviceId = :deviceId and s.active = true")
    int deactivateForDevice(@Param("userId") UUID userId, @Param("deviceId") String deviceId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update UserSessionEntity s set s.active = false where s.userId = :userId and s.active = true")
    int deactivateAllForUser(@Param("userId") UUID userId);
}