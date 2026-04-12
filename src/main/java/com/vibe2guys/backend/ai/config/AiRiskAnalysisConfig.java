package com.vibe2guys.backend.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiRiskAnalysisProperties.class)
public class AiRiskAnalysisConfig {
}
