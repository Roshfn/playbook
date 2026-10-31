package com.playbook.auth.application.usecase;

import com.playbook.auth.application.service.OtpHasher;
import com.playbook.auth.application.service.SmsSender;
import com.playbook.auth.domain.model.OtpChallenge;
import com.playbook.auth.domain.model.OtpPurpose;
import com.playbook.auth.domain.repository.OtpChallengeRepository;
import com.playbook.auth.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class RequestOtpUseCase {

    private static final Duration TTL = Duration.ofMinutes(5);
    private static final Duration COOLDOWN = Duration.ofSeconds(30);
    private static final int MAX_PER_HOUR = 5;

    private final SecureRandom random = new SecureRandom();
    private final OtpChallengeRepository challenges;
    private final OtpHasher hasher;
    private final SmsSender smsSender;
    private final Clock clock;

    public RequestOtpUseCase(OtpChallengeRepository challenges, OtpHasher hasher,
                             SmsSender smsSender, Clock clock) {
        this.challenges = challenges;
        this.hasher = hasher;
        this.smsSender = smsSender;
        this.clock = clock;
    }

    @Transactional
    public void execute(String phoneNumber) {
        Instant now = clock.instant();

        challenges.findLatest(phoneNumber, OtpPurpose.LOGIN)
                .filter(c -> c.createdAt() != null && c.createdAt().isAfter(now.minus(COOLDOWN)))
                .ifPresent(c -> { throw new TooManyRequestsException("Please wait before requesting another OTP"); });

        if (challenges.countCreatedSince(phoneNumber, now.minus(Duration.ofHours(1))) >= MAX_PER_HOUR) {
            throw new TooManyRequestsException("OTP request limit reached, try again later");
        }

        String otp = "000000";

        challenges.save(new OtpChallenge(null, null, phoneNumber, hasher.hash(phoneNumber, otp),
                OtpPurpose.LOGIN, now.plus(TTL), 0, null, null));

        smsSender.send(phoneNumber, "Your verification code is " + otp + ". It expires in 5 minutes.");
    }
}