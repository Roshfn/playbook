package com.playbook.auth.application.service;

public interface OtpHasher {
    String hash(String phoneNumber, String otp);
    boolean matches(String phoneNumber, String otp, String storedHash);
}