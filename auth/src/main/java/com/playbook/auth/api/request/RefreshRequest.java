package com.playbook.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(@NotBlank String refreshToken, @NotBlank @Size(max = 100) String deviceId) {}