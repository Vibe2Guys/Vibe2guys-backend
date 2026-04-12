package com.vibe2guys.backend.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.s3")
public record S3StorageProperties(
        boolean enabled,
        String region,
        String bucket,
        String accessKey,
        String secretKey,
        String uploadPrefix,
        String publicBaseUrl,
        long presignExpirationSeconds
) {
}
