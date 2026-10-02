package com.playbook.auth.application.usecase;

import com.playbook.auth.application.service.*;
import com.playbook.auth.domain.model.AuthenticationCommand;
import com.playbook.auth.domain.model.User;
import com.playbook.auth.domain.model.VerifiedIdentity;
import com.playbook.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class LoginUseCase {

    private final AuthenticationStrategyRegistry registry;
    private final UserResolutionService userResolution;
    private final TokenIssuer tokenIssuer;
    private final UserRepository users;
    private final Clock clock;

    public LoginUseCase(AuthenticationStrategyRegistry registry,
                        UserResolutionService userResolution,
                        TokenIssuer tokenIssuer,
                        UserRepository users,
                        Clock clock) {
        this.registry = registry;
        this.userResolution = userResolution;
        this.tokenIssuer = tokenIssuer;
        this.users = users;
        this.clock = clock;
    }

    @Transactional
    public AuthTokens execute(AuthenticationCommand command) {
        VerifiedIdentity identity = registry.get(command.provider()).authenticate(command);
        User user = userResolution.resolve(identity);
        users.updateLastLogin(user.id(), clock.instant());
        return tokenIssuer.issue(user, command.deviceId(), command.ipAddress(), command.userAgent());
    }
}