package com.playbook.auth.application.usecase;

import com.playbook.auth.application.service.AuthTokens;
import com.playbook.auth.application.service.TokenIssuer;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenUseCase {

    private final TokenIssuer tokenIssuer;

    public RefreshTokenUseCase(TokenIssuer tokenIssuer) { this.tokenIssuer = tokenIssuer; }

    public AuthTokens execute(String refreshToken, String deviceId, String ip, String userAgent) {
        return tokenIssuer.rotate(refreshToken, deviceId, ip, userAgent);
    }
}