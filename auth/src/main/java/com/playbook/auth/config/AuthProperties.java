package com.playbook.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(Oidc oidc, Otp otp) {
    public record Oidc(Provider google, Provider apple) {}
    public record Provider(Set<String> clientIds) {}
    public record Otp(String pepper) {}
}