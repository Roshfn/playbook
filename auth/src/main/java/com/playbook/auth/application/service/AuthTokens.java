package com.playbook.auth.application.service;

public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds) {}