package com.playbook.auth.infrastructure.persistance.repository;

import com.playbook.auth.domain.model.OtpChallenge;
import com.playbook.auth.domain.model.OtpPurpose;
import com.playbook.auth.domain.repository.OtpChallengeRepository;
import com.playbook.auth.infrastructure.persistance.entity.OtpChallengeEntity;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public class OtpChallengeRepositoryAdapter implements OtpChallengeRepository {

    private final OtpChallengeJpaRepository jpa;

    public OtpChallengeRepositoryAdapter(OtpChallengeJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public Optional<OtpChallenge> findLatestActive(String phone, OtpPurpose purpose, Instant now) {
        return jpa.findFirstByPhoneNumberAndPurposeAndVerifiedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(phone, purpose, now)
                .map(this::toDomain);
    }

    public Optional<OtpChallenge> findLatest(String phone, OtpPurpose purpose) {
        return jpa.findFirstByPhoneNumberAndPurposeOrderByCreatedAtDesc(phone, purpose).map(this::toDomain);
    }

    public long countCreatedSince(String phone, Instant since) {
        return jpa.countByPhoneNumberAndCreatedAtAfter(phone, since);
    }

    @Override
    public OtpChallenge save(OtpChallenge c) {
        OtpChallengeEntity e = new OtpChallengeEntity();
        e.setId(c.id());
        e.setUserId(c.userId());
        e.setPhoneNumber(c.phoneNumber());
        e.setOtpHash(c.otpHash());
        e.setPurpose(c.purpose());
        e.setExpiresAt(c.expiresAt());
        e.setAttempts(c.attempts());
        e.setVerifiedAt(c.verifiedAt());
        return toDomain(jpa.saveAndFlush(e));
    }

    private OtpChallenge toDomain(OtpChallengeEntity e) {
        return new OtpChallenge(e.getId(), e.getUserId(), e.getPhoneNumber(), e.getOtpHash(),
                e.getPurpose(), e.getExpiresAt(), e.getAttempts(), e.getVerifiedAt(), e.getCreatedAt());
    }
}