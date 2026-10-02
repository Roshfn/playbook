package com.playbook.auth.infrastructure.security;

import com.playbook.auth.config.AuthProperties;
import com.playbook.auth.domain.model.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AppleAuthenticationStrategy extends OidcAuthenticationStrategy {

    public AppleAuthenticationStrategy(AuthProperties props) {
        super(AuthProvider.APPLE, new OidcIdTokenVerifier(
                "https://appleid.apple.com/auth/keys",
                Set.of("https://appleid.apple.com"),
                props.oidc().apple().clientIds()));
    }
}