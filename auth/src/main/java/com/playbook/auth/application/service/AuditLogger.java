package com.playbook.auth.application.service;

import com.playbook.auth.domain.model.AuthProvider;
import java.util.UUID;

public interface AuditLogger {
    void loginSuccess(UUID userId, AuthProvider provider, String ip, String userAgent);
    void loginFailure(AuthProvider provider, String ip, String userAgent);
}