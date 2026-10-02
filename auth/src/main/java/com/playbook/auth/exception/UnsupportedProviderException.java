package com.playbook.auth.exception;

public class UnsupportedProviderException extends RuntimeException {
    public UnsupportedProviderException(String provider) { super("Unsupported provider: " + provider); }
}