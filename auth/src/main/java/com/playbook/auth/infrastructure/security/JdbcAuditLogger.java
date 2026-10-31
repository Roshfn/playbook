package com.playbook.auth.infrastructure.security;

import com.playbook.auth.application.service.AuditLogger;
import com.playbook.auth.domain.model.AuthProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class JdbcAuditLogger implements AuditLogger {

    private static final String SQL = """
            INSERT INTO audit_logs (user_id, event_type, provider, ip_address, user_agent, success)
            VALUES (?, ?, ?, ?::inet, ?, ?)
            """;

    private final JdbcTemplate jdbc;

    public JdbcAuditLogger(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    // joins the login transaction: the user row may not be committed yet (FK)
    @Override
    public void loginSuccess(UUID userId, AuthProvider provider, String ip, String userAgent) {
        jdbc.update(SQL, userId, "LOGIN_SUCCESS", provider.name(), ip, userAgent, true);
    }

    // own transaction: must survive the rollback of a failed login
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loginFailure(AuthProvider provider, String ip, String userAgent) {
        jdbc.update(SQL, null, "LOGIN_FAILURE", provider.name(), ip, userAgent, false);
    }
}