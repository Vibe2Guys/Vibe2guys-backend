package com.vibe2guys.backend.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe2guys.backend.ai.config.AiRiskAnalysisProperties;
import com.vibe2guys.backend.analytics.domain.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRiskAnalysisService {

    private final AiRiskAnalysisProperties properties;
    private final ObjectMapper objectMapper;

    public AiRiskAssessment assessStudentRisk(Map<String, Object> input) {
        if (!properties.enabled()
                || properties.apiKey() == null || properties.apiKey().isBlank()
                || properties.baseUrl() == null || properties.baseUrl().isBlank()
                || properties.model() == null || properties.model().isBlank()) {
            return null;
        }

        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(properties.timeoutSeconds() * 1000);
            requestFactory.setReadTimeout(properties.timeoutSeconds() * 1000);
            RestClient client = RestClient.builder()
                    .baseUrl(properties.baseUrl())
                    .requestFactory(requestFactory)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            Map<String, Object> request = new HashMap<>();
            request.put("model", properties.model());
            request.put("temperature", properties.temperature());
            request.put("response_format", Map.of("type", "json_object"));
            request.put("messages", List.of(
                    Map.of("role", "system", "content", loadSystemPrompt()),
                    Map.of("role", "user", "content", buildUserPrompt(input))
            ));

            JsonNode response = client.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            String content = response.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                return null;
            }
            JsonNode json = objectMapper.readTree(content);
            int riskScore = clamp(json.path("riskScore").asInt(50));
            RiskLevel riskLevel = RiskLevel.valueOf(json.path("riskLevel").asText("MEDIUM").trim().toUpperCase());
            List<String> reasons = objectMapper.convertValue(json.path("reasons"), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
            List<String> recommendations = objectMapper.convertValue(json.path("recommendations"), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
            String coachingMessage = json.path("coachingMessage").asText("학습 상태를 계속 관찰하세요.");
            return new AiRiskAssessment(
                    riskScore,
                    riskLevel,
                    reasons == null ? List.of() : reasons,
                    coachingMessage,
                    recommendations == null ? List.of() : recommendations
            );
        } catch (Exception ex) {
            log.warn("AI risk analysis failed, falling back to rule-based analytics", ex);
            return null;
        }
    }

    private String loadSystemPrompt() throws IOException {
        return new ClassPathResource("prompts/ai-risk-analysis-system.txt")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String buildUserPrompt(Map<String, Object> input) throws IOException {
        return "학생 학습 분석 입력 데이터:\n" + objectMapper.writeValueAsString(input);
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
