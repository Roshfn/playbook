package com.playbook.auth.application.service;

import com.playbook.auth.domain.model.User;

public interface TokenIssuer {
    AuthTokens issue(User user, String deviceId, String ipAddress, String userAgent);
    AuthTokens rotate(String refreshToken, String deviceId, String ipAddress, String userAgent);
    void revoke(String refreshToken);
}