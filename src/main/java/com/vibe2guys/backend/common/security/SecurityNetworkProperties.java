package com.vibe2guys.backend.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security.network")
public record SecurityNetworkProperties(
        boolean trustProxyHeaders,
        List<String> allowedOrigins
) {
    public SecurityNetworkProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
