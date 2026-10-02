package com.playbook.auth.infrastructure.security;

import com.playbook.auth.application.service.OtpHasher;
import com.playbook.auth.domain.model.*;
import com.playbook.auth.domain.repository.OtpChallengeRepository;
import com.playbook.auth.domain.strategy.AuthenticationStrategy;
import com.playbook.auth.exception.AuthenticationFailedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
public class MobileOtpAuthenticationStrategy implements AuthenticationStrategy {

    private static final int MAX_ATTEMPTS = 5;

    private final OtpChallengeRepository challenges;
    private final OtpHasher hasher;
    private final Clock clock;

    public MobileOtpAuthenticationStrategy(OtpChallengeRepository challenges, OtpHasher hasher, Clock clock) {
        this.challenges = challenges;
        this.hasher = hasher;
        this.clock = clock;
    }

    @Override
    public AuthProvider provider() { return AuthProvider.MOBILE_OTP; }

    // failed attempts must be persisted even though we throw
    @Override
    @Transactional(noRollbackFor = AuthenticationFailedException.class)
    public VerifiedIdentity authenticate(AuthenticationCommand command) {
        String phone = command.phoneNumber();
        String otp = command.otp();
        if (phone == null || phone.isBlank() || otp == null || otp.isBlank()) {
            throw new AuthenticationFailedException("phoneNumber and otp are required");
        }

        Instant now = clock.instant();
        OtpChallenge challenge = challenges.findLatestActive(phone, OtpPurpose.LOGIN, now)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid or expired OTP"));

        if (challenge.attempts() >= MAX_ATTEMPTS) {
            throw new AuthenticationFailedException("Too many attempts, request a new OTP");
        }

        if (!hasher.matches(phone, otp, challenge.otpHash())) {
            challenges.save(challenge.withFailedAttempt());
            throw new AuthenticationFailedException("Invalid or expired OTP");
        }

        challenges.save(challenge.markVerified(now));

        return new VerifiedIdentity(AuthProvider.MOBILE_OTP, phone, null, false, phone,
                null, null, null, null);
    }
}