package com.vibe2guys.backend.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.network")
public record SecurityNetworkProperties(
        boolean trustProxyHeaders
) {
}
