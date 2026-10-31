package com.playbook.auth.application.usecase;

import com.playbook.auth.application.service.*;
import com.playbook.auth.domain.model.AuthenticationCommand;
import com.playbook.auth.domain.model.User;
import com.playbook.auth.domain.model.VerifiedIdentity;
import com.playbook.auth.domain.repository.UserRepository;
import com.playbook.auth.exception.AccountNotActiveException;
import com.playbook.auth.exception.AuthenticationFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class LoginUseCase {

    private final AuthenticationStrategyRegistry registry;
    private final UserResolutionService userResolution;
    private final TokenIssuer tokenIssuer;
    private final UserRepository users;
    private final AuditLogger audit;
    private final Clock clock;

    public LoginUseCase(AuthenticationStrategyRegistry registry, UserResolutionService userResolution,
                        TokenIssuer tokenIssuer, UserRepository users, AuditLogger audit, Clock clock) {
        this.registry = registry;
        this.userResolution = userResolution;
        this.tokenIssuer = tokenIssuer;
        this.users = users;
        this.audit = audit;
        this.clock = clock;
    }

    // noRollbackFor: the OTP attempt counter must survive a failed login
    @Transactional(noRollbackFor = AuthenticationFailedException.class)
    public AuthTokens execute(AuthenticationCommand command) {
        try {
            VerifiedIdentity identity = registry.get(command.provider()).authenticate(command);
            User user = userResolution.resolve(identity);
            users.updateLastLogin(user.id(), clock.instant());
            AuthTokens tokens = tokenIssuer.issue(user, command.deviceId(), command.ipAddress(), command.userAgent());
            audit.loginSuccess(user.id(), command.provider(), command.ipAddress(), command.userAgent());
            return tokens;
        } catch (AuthenticationFailedException | AccountNotActiveException e) {
            audit.loginFailure(command.provider(), command.ipAddress(), command.userAgent());
            throw e;
        }
    }
}