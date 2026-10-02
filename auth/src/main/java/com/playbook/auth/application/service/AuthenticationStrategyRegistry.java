package com.playbook.auth.application.service;

import com.playbook.auth.domain.model.AuthProvider;
import com.playbook.auth.domain.strategy.AuthenticationStrategy;
import com.playbook.auth.exception.UnsupportedProviderException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthenticationStrategyRegistry {

    private final Map<AuthProvider, AuthenticationStrategy> strategies = new EnumMap<>(AuthProvider.class);

    public AuthenticationStrategyRegistry(List<AuthenticationStrategy> all) {
        for (AuthenticationStrategy s : all) {
            if (strategies.putIfAbsent(s.provider(), s) != null) {
                throw new IllegalStateException("Duplicate strategy for " + s.provider());
            }
        }
    }

    public AuthenticationStrategy get(AuthProvider provider) {
        AuthenticationStrategy s = strategies.get(provider);
        if (s == null) throw new UnsupportedProviderException(String.valueOf(provider));
        return s;
    }
}