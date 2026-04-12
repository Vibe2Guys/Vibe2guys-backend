package com.vibe2guys.backend.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.risk-analysis")
public record AiRiskAnalysisProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        double temperature,
        int timeoutSeconds
) {
}
