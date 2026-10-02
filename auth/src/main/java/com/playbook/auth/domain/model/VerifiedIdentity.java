package com.playbook.auth.domain.model;

/** Output of every strategy: a provider-verified identity. No DB, no tokens. */
public record VerifiedIdentity(
        AuthProvider provider,
        String providerUserId,
        String email,
        boolean emailVerified,
        String phoneNumber,
        String firstName,
        String lastName,
        String displayName,
        String pictureUrl) {}