package com.playbook.auth.domain.strategy;

import com.playbook.auth.domain.model.AuthProvider;
import com.playbook.auth.domain.model.AuthenticationCommand;
import com.playbook.auth.domain.model.VerifiedIdentity;

public interface AuthenticationStrategy {

    AuthProvider provider();

    /** Verifies credentials and returns the proven identity. Throws AuthenticationFailedException on failure. */
    VerifiedIdentity authenticate(AuthenticationCommand command);
}