package com.playbook.auth.infrastructure.persistance.entity;

import com.playbook.auth.domain.model.AuthProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_identities")
@Getter @Setter @NoArgsConstructor
public class UserIdentityEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID userId;
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    private String providerUserId;
    private String email;
    private String displayName;
    private String profilePicture;
}