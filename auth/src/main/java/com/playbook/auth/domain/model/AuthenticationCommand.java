package com.playbook.auth.domain.model;

public record AuthenticationCommand(
        AuthProvider provider,
        String idToken,
        String phoneNumber,
        String otp,
        String deviceId,
        String ipAddress,
        String userAgent) {}