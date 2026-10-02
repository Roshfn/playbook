package com.playbook.auth.domain.repository;

import com.playbook.auth.domain.model.AuthProvider;
import com.playbook.auth.domain.model.UserIdentity;
import java.util.Optional;

public interface UserIdentityRepository {
    Optional<UserIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
    UserIdentity save(UserIdentity identity);
}