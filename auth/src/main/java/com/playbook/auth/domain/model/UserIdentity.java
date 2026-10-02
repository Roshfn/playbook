package com.playbook.auth.domain.model;

import java.util.UUID;

public record UserIdentity(
        UUID id,
        UUID userId,
        AuthProvider provider,
        String providerUserId,
        String email,
        String displayName,
        String profilePicture) {}