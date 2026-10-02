package com.playbook.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public record OtpChallenge(
        UUID id,
        UUID userId,
        String phoneNumber,
        String otpHash,
        OtpPurpose purpose,
        Instant expiresAt,
        int attempts,
        Instant verifiedAt,
        Instant createdAt) {

    public OtpChallenge withFailedAttempt() {
        return new OtpChallenge(id, userId, phoneNumber, otpHash, purpose, expiresAt, attempts + 1, verifiedAt, createdAt);
    }

    public OtpChallenge markVerified(Instant at) {
        return new OtpChallenge(id, userId, phoneNumber, otpHash, purpose, expiresAt, attempts, at, createdAt);
    }
}