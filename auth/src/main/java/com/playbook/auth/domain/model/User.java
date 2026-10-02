package com.playbook.auth.domain.model;

import java.util.UUID;

public record User(
        UUID id,
        String email,
        String phone,
        String firstName,
        String lastName,
        UserStatus status,
        boolean emailVerified,
        boolean phoneVerified) {

    public boolean isActive() { return status == UserStatus.ACTIVE; }

    public User withPhoneVerified(String verifiedPhone) {
        return new User(id, email, verifiedPhone, firstName, lastName, status, emailVerified, true);
    }
}