package com.playbook.auth.domain.repository;

import java.util.Set;
import java.util.UUID;

public interface UserAuthorityRepository {
    void assignRole(UUID userId, String roleName);
    Set<String> findRoleNames(UUID userId);
    Set<String> findPermissionNames(UUID userId);
}