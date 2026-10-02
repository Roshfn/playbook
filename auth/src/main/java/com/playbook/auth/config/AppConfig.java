package com.playbook.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AppConfig {

    @Bean
    Clock clock() { return Clock.systemUTC(); }
}