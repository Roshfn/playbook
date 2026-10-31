package com.playbook.auth.infrastructure.persistance.repository;

import com.playbook.auth.domain.model.User;
import com.playbook.auth.domain.repository.UserRepository;
import com.playbook.auth.infrastructure.persistance.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) { this.jpa = jpa; }

    @Override public Optional<User> findById(UUID id) { return jpa.findById(id).map(this::toDomain); }
    @Override public Optional<User> findByEmail(String email) { return jpa.findByEmailIgnoreCase(email).map(this::toDomain); }
    @Override public Optional<User> findByPhone(String phone) { return jpa.findByPhone(phone).map(this::toDomain); }

    // flush so later JDBC statements (user_roles) can see the row
    @Override public User save(User u) { return toDomain(jpa.saveAndFlush(toEntity(u))); }

    @Override public void updateLastLogin(UUID userId, Instant at) { jpa.updateLastLogin(userId, at); }

    private User toDomain(UserEntity e) {
        return new User(e.getId(), e.getEmail(), e.getPhone(), e.getFirstName(), e.getLastName(),
                e.getStatus(), e.isEmailVerified(), e.isPhoneVerified());
    }

    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.setId(u.id());
        e.setEmail(u.email());
        e.setPhone(u.phone());
        e.setFirstName(u.firstName());
        e.setLastName(u.lastName());
        e.setStatus(u.status());
        e.setEmailVerified(u.emailVerified());
        e.setPhoneVerified(u.phoneVerified());
        return e;
    }
}