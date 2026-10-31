package com.playbook.auth.infrastructure.persistance.repository;

import com.playbook.auth.domain.model.AuthProvider;
import com.playbook.auth.domain.model.UserIdentity;
import com.playbook.auth.domain.repository.UserIdentityRepository;
import com.playbook.auth.infrastructure.persistance.entity.UserIdentityEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserIdentityRepositoryAdapter implements UserIdentityRepository {

    private final UserIdentityJpaRepository jpa;

    public UserIdentityRepositoryAdapter(UserIdentityJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Optional<UserIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return jpa.findByProviderAndProviderUserId(provider, providerUserId).map(this::toDomain);
    }

    @Override
    public UserIdentity save(UserIdentity i) {
        UserIdentityEntity e = new UserIdentityEntity();
        e.setId(i.id());
        e.setUserId(i.userId());
        e.setProvider(i.provider());
        e.setProviderUserId(i.providerUserId());
        e.setEmail(i.email());
        e.setDisplayName(i.displayName());
        e.setProfilePicture(i.profilePicture());
        return toDomain(jpa.saveAndFlush(e));
    }

    private UserIdentity toDomain(UserIdentityEntity e) {
        return new UserIdentity(e.getId(), e.getUserId(), e.getProvider(), e.getProviderUserId(),
                e.getEmail(), e.getDisplayName(), e.getProfilePicture());
    }
}