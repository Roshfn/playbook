package com.playbook.auth.infrastructure.security;

import com.playbook.auth.application.service.AuthTokens;
import com.playbook.auth.application.service.TokenIssuer;
import com.playbook.auth.config.AuthProperties;
import com.playbook.auth.domain.model.User;
import com.playbook.auth.domain.repository.UserAuthorityRepository;
import com.playbook.auth.domain.repository.UserRepository;
import com.playbook.auth.exception.AuthenticationFailedException;
import com.playbook.auth.infrastructure.persistance.entity.RefreshTokenEntity;
import com.playbook.auth.infrastructure.persistance.entity.UserSessionEntity;
import com.playbook.auth.infrastructure.persistance.repository.RefreshTokenJpaRepository;
import com.playbook.auth.infrastructure.persistance.repository.UserSessionJpaRepository;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenIssuer implements TokenIssuer {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JwtEncoder encoder;
    private final AuthProperties props;
    private final Clock clock;
    private final RefreshTokenJpaRepository refreshTokens;
    private final UserSessionJpaRepository sessions;
    private final UserRepository users;
    private final UserAuthorityRepository authorities;

    public JwtTokenIssuer(JwtEncoder encoder, AuthProperties props, Clock clock,
                          RefreshTokenJpaRepository refreshTokens, UserSessionJpaRepository sessions,
                          UserRepository users, UserAuthorityRepository authorities) {
        this.encoder = encoder;
        this.props = props;
        this.clock = clock;
        this.refreshTokens = refreshTokens;
        this.sessions = sessions;
        this.users = users;
        this.authorities = authorities;
    }

    /** Fresh login: one active session per (user, device). */
    @Override
    @Transactional
    public AuthTokens issue(User user, String deviceId, String ip, String userAgent) {
        Instant now = clock.instant();
        refreshTokens.revokeActiveForDevice(user.id(), deviceId, now);
        sessions.deactivateForDevice(user.id(), deviceId);

        String rawRefresh = newRawToken();
        RefreshTokenEntity rt = saveRefreshToken(user.id(), deviceId, rawRefresh, now);

        UserSessionEntity s = new UserSessionEntity();
        s.setUserId(user.id());
        s.setRefreshTokenId(rt.getId());
        s.setDeviceId(deviceId);
        s.setIpAddress(ip);
        s.setUserAgent(truncate(userAgent, 500));
        s.setActive(true);
        s.setLastActivityAt(now);
        sessions.save(s);

        return new AuthTokens(accessToken(user, deviceId, now), rawRefresh, props.jwt().accessTtlSeconds());
    }

    /** Refresh-token rotation with reuse detection. */
    @Override
    @Transactional(noRollbackFor = AuthenticationFailedException.class)
    public AuthTokens rotate(String rawRefreshToken, String deviceId, String ip, String userAgent) {
        Instant now = clock.instant();

        RefreshTokenEntity old = refreshTokens.findByTokenHash(sha256(rawRefreshToken))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token"));

        if (old.getRevokedAt() != null) {
            // an already-used token came back: assume theft, kill every session of this user
            refreshTokens.revokeAllForUser(old.getUserId(), now);
            sessions.deactivateAllForUser(old.getUserId());
            throw new AuthenticationFailedException("Refresh token reuse detected");
        }
        if (!old.getDeviceId().equals(deviceId) || old.getExpiresAt().isBefore(now)) {
            throw new AuthenticationFailedException("Invalid refresh token");
        }

        User user = users.findById(old.getUserId()).filter(User::isActive)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid refresh token"));

        old.setRevokedAt(now);
        String rawNext = newRawToken();
        RefreshTokenEntity next = saveRefreshToken(user.id(), deviceId, rawNext, now);

        UserSessionEntity session = sessions.findByRefreshTokenId(old.getId()).orElseGet(() -> {
            UserSessionEntity s = new UserSessionEntity();
            s.setUserId(user.id());
            s.setDeviceId(deviceId);
            return s;
        });
        session.setRefreshTokenId(next.getId());
        session.setIpAddress(ip);
        session.setUserAgent(truncate(userAgent, 500));
        session.setActive(true);
        session.setLastActivityAt(now);
        sessions.save(session);

        return new AuthTokens(accessToken(user, deviceId, now), rawNext, props.jwt().accessTtlSeconds());
    }

    @Override
    @Transactional
    public void revoke(String rawRefreshToken) {
        Instant now = clock.instant();
        refreshTokens.findByTokenHash(sha256(rawRefreshToken)).ifPresent(rt -> {
            if (rt.getRevokedAt() == null) rt.setRevokedAt(now);
            sessions.findByRefreshTokenId(rt.getId()).ifPresent(s -> s.setActive(false));
        });
    }

    private RefreshTokenEntity saveRefreshToken(UUID userId, String deviceId, String raw, Instant now) {
        RefreshTokenEntity e = new RefreshTokenEntity();
        e.setUserId(userId);
        e.setTokenHash(sha256(raw));
        e.setDeviceId(deviceId);
        e.setExpiresAt(now.plusSeconds(props.jwt().refreshTtlSeconds()));
        return refreshTokens.saveAndFlush(e);
    }

    private String accessToken(User user, String deviceId, Instant now) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.jwt().issuer())
                .subject(user.id().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(props.jwt().accessTtlSeconds()))
                .id(UUID.randomUUID().toString())
                .claim("roles", List.copyOf(authorities.findRoleNames(user.id())))
                .claim("permissions", List.copyOf(authorities.findPermissionNames(user.id())))
                .claim("did", deviceId)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(props.jwt().keyId()).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static String newRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}