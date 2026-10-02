package com.playbook.auth.application.service;

import com.playbook.auth.domain.model.User;

/** Port. Implementation (JWT + refresh token + session) comes in infrastructure/security. */
public interface TokenIssuer {
    AuthTokens issue(User user, String deviceId, String ipAddress, String userAgent);
}