package com.playbook.auth.infrastructure.security;

import com.playbook.auth.application.service.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Dev only. For prod, add another SmsSender implementation (MSG91 / Twilio / SNS). */
@Slf4j
@Component
@Profile("dev")
public class LoggingSmsSender implements SmsSender {
    @Override
    public void send(String phoneNumber, String message) {
        log.warn("DEV SMS to {}: {}", phoneNumber, message);
    }
}