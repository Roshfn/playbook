package com.playbook.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.util.Set;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(Oidc oidc, Otp otp, JwtSettings jwt) {
    public record Oidc(Provider google, Provider apple) {}
    public record Provider(Set<String> clientIds) {}
    public record Otp(String pepper) {}
    public record JwtSettings(Resource privateKey, Resource publicKey, String issuer, String keyId,
                              long accessTtlSeconds, long refreshTtlSeconds) {}
}