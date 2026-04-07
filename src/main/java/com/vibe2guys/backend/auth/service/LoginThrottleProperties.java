package com.vibe2guys.backend.auth.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.login-throttle")
public record LoginThrottleProperties(
        int maxAttempts,
        long windowSeconds,
        long blockSeconds
) {
}
