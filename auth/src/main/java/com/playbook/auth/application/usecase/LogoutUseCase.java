package com.playbook.auth.application.usecase;

import com.playbook.auth.application.service.TokenIssuer;
import org.springframework.stereotype.Service;

@Service
public class LogoutUseCase {

    private final TokenIssuer tokenIssuer;

    public LogoutUseCase(TokenIssuer tokenIssuer) { this.tokenIssuer = tokenIssuer; }

    public void execute(String refreshToken) { tokenIssuer.revoke(refreshToken); }
}