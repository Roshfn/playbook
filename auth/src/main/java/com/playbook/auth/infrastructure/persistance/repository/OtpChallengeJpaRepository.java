package com.playbook.auth.infrastructure.persistance.repository;

import com.playbook.auth.domain.model.OtpPurpose;
import com.playbook.auth.infrastructure.persistance.entity.OtpChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeJpaRepository extends JpaRepository<OtpChallengeEntity, UUID> {

    Optional<OtpChallengeEntity>
    findFirstByPhoneNumberAndPurposeAndVerifiedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            String phoneNumber, OtpPurpose purpose, Instant now);

    Optional<OtpChallengeEntity> findFirstByPhoneNumberAndPurposeOrderByCreatedAtDesc(
            String phoneNumber, OtpPurpose purpose);

    long countByPhoneNumberAndCreatedAtAfter(String phoneNumber, Instant since);
}