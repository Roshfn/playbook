package com.playbook.auth.api.request;

import com.playbook.auth.domain.model.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotNull AuthProvider provider,
        String idToken,
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "phoneNumber must be E.164") String phoneNumber,
        String otp,
        @NotBlank @Size(max = 100) String deviceId) {}