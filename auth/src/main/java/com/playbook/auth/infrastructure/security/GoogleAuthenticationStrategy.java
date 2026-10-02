package com.playbook.auth.infrastructure.security;

import com.playbook.auth.config.AuthProperties;
import com.playbook.auth.domain.model.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class GoogleAuthenticationStrategy extends OidcAuthenticationStrategy {

    public GoogleAuthenticationStrategy(AuthProperties props) {
        super(AuthProvider.GOOGLE, new OidcIdTokenVerifier(
                "https://www.googleapis.com/oauth2/v3/certs",
                Set.of("https://accounts.google.com", "accounts.google.com"),
                props.oidc().google().clientIds()));
    }
}