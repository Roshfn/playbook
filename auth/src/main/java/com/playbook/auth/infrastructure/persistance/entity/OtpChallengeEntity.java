package com.playbook.auth.infrastructure.persistance.entity;

import com.playbook.auth.domain.model.OtpPurpose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_challenges")
@Getter @Setter @NoArgsConstructor
public class OtpChallengeEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    private String phoneNumber;
    private String otpHash;
    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;
    private Instant expiresAt;
    private int attempts;
    private Instant verifiedAt;
    @Column(insertable = false, updatable = false)
    private Instant createdAt;
}