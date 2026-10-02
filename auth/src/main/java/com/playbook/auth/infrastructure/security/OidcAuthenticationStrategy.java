package com.playbook.auth.infrastructure.security;

import com.playbook.auth.domain.model.AuthProvider;
import com.playbook.auth.domain.model.AuthenticationCommand;
import com.playbook.auth.domain.model.VerifiedIdentity;
import com.playbook.auth.domain.strategy.AuthenticationStrategy;
import com.playbook.auth.exception.AuthenticationFailedException;
import org.springframework.security.oauth2.jwt.Jwt;

public abstract class OidcAuthenticationStrategy implements AuthenticationStrategy {

    private final AuthProvider provider;
    private final OidcIdTokenVerifier verifier;

    protected OidcAuthenticationStrategy(AuthProvider provider, OidcIdTokenVerifier verifier) {
        this.provider = provider;
        this.verifier = verifier;
    }

    @Override
    public AuthProvider provider() { return provider; }

    @Override
    public VerifiedIdentity authenticate(AuthenticationCommand command) {
        if (command.idToken() == null || command.idToken().isBlank()) {
            throw new AuthenticationFailedException("idToken is required");
        }
        Jwt jwt = verifier.verify(command.idToken());

        // Google returns a boolean, Apple may return the string "true"
        boolean emailVerified = Boolean.parseBoolean(String.valueOf(jwt.getClaim("email_verified")));

        return new VerifiedIdentity(
                provider,
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                emailVerified,
                null,
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name"),
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("picture"));
    }
}