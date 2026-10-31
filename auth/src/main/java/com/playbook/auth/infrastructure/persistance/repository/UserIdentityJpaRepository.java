package com.playbook.auth.infrastructure.persistance.repository;

import com.playbook.auth.domain.model.AuthProvider;
import com.playbook.auth.infrastructure.persistance.entity.UserIdentityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserIdentityJpaRepository extends JpaRepository<UserIdentityEntity, UUID> {
    Optional<UserIdentityEntity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}