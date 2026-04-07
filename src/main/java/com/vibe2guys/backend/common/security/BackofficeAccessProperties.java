package com.vibe2guys.backend.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.backoffice")
public record BackofficeAccessProperties(
        String pathPrefix,
        String headerName,
        String accessKey
) {
}
