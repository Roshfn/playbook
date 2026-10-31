package com.playbook.auth.application.service;

public interface SmsSender {
    void send(String phoneNumber, String message);
}