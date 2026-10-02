package com.playbook.auth.api.response;

import com.playbook.auth.application.service.AuthTokens;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
    public static AuthResponse from(AuthTokens t) {
        return new AuthResponse(t.accessToken(), t.refreshToken(), "Bearer", t.expiresInSeconds());
    }
}