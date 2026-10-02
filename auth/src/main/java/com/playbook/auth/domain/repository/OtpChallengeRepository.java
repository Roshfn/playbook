package com.playbook.auth.domain.repository;

import com.playbook.auth.domain.model.OtpChallenge;
import com.playbook.auth.domain.model.OtpPurpose;
import java.time.Instant;
import java.util.Optional;

public interface OtpChallengeRepository {
    Optional<OtpChallenge> findLatestActive(String phoneNumber, OtpPurpose purpose, Instant now);
    OtpChallenge save(OtpChallenge challenge);
}