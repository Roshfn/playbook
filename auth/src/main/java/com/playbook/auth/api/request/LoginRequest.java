package com.playbook.auth.api.request;

import com.playbook.auth.domain.model.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull AuthProvider provider,
        String idToken,
        String phoneNumber,
        String otp,
        @NotBlank String deviceId) {}