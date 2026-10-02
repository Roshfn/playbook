package com.playbook.auth.infrastructure.security;

import com.playbook.auth.exception.AuthenticationFailedException;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class OidcIdTokenVerifier {

    private final NimbusJwtDecoder decoder;

    public OidcIdTokenVerifier(String jwkSetUri, Set<String> issuers, Set<String> audiences) {
        NimbusJwtDecoder d = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtClaimValidator<Object>("iss", iss -> iss != null && issuers.contains(iss.toString())),
                new JwtClaimValidator<Collection<String>>("aud",
                        aud -> aud != null && aud.stream().anyMatch(audiences::contains)));

        d.setJwtValidator(validator);
        this.decoder = d;
    }

    public Jwt verify(String idToken) {
        try {
            return decoder.decode(idToken);
        } catch (JwtException e) {
            throw new AuthenticationFailedException("Invalid ID token", e);
        }
    }
}