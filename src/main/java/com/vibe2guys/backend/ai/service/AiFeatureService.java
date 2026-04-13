package com.vibe2guys.backend.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibe2guys.backend.ai.config.AiRiskAnalysisProperties;
import com.vibe2guys.backend.ai.domain.FollowUpContextType;
import com.vibe2guys.backend.ai.domain.FollowUpDifficultyLevel;
import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.team.domain.TeamLearningStyle;
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
public class AiFeatureService {

    private final AiRiskAnalysisProperties properties;
    private final ObjectMapper objectMapper;

    public boolean isConfigured() {
        return properties.enabled()
                && properties.apiKey() != null && !properties.apiKey().isBlank()
                && properties.baseUrl() != null && !properties.baseUrl().isBlank()
                && properties.model() != null && !properties.model().isBlank();
    }

    public AiGeneratedFollowUpQuestion generateFollowUpQuestion(
            FollowUpContextType contextType,
            Course course,
            Content content,
            String sourceText,
            FollowUpDifficultyLevel fallbackDifficulty
    ) {
        Map<String, Object> input = new HashMap<>();
        input.put("task", "follow_up_question");
        input.put("contextType", contextType.name());
        input.put("courseTitle", course.getTitle());
        input.put("courseDescription", course.getDescription());
        input.put("contentTitle", content == null ? null : content.getTitle());
        input.put("contentType", content == null ? null : content.getType().name());
        input.put("sourceText", sourceText);
        input.put("fallbackDifficulty", fallbackDifficulty.name());
        JsonNode json = requestJson("prompts/ai-follow-up-question-system.txt", input);
        if (json == null) {
            return null;
        }
        String questionText = json.path("questionText").asText("").trim();
        if (questionText.isBlank()) {
            return null;
        }
        String difficultyValue = json.path("difficultyLevel").asText(fallbackDifficulty.name()).trim().toUpperCase();
        FollowUpDifficultyLevel difficultyLevel = parseDifficulty(difficultyValue, fallbackDifficulty);
        return new AiGeneratedFollowUpQuestion(questionText, difficultyLevel);
    }

    public AiGeneratedFollowUpAnalysis analyzeFollowUpResponse(
            String questionText,
            String sourceText,
            String answerText,
            int responseDelaySeconds
    ) {
        Map<String, Object> input = new HashMap<>();
        input.put("task", "follow_up_analysis");
        input.put("questionText", questionText);
        input.put("sourceText", sourceText);
        input.put("answerText", answerText);
        input.put("responseDelaySeconds", responseDelaySeconds);
        JsonNode json = requestJson("prompts/ai-follow-up-analysis-system.txt", input);
        if (json == null) {
            return null;
        }
        int understandingScore = clamp(json.path("understandingScore").asInt(-1));
        String feedback = json.path("feedback").asText("").trim();
        if (understandingScore < 0 || feedback.isBlank()) {
            return null;
        }
        return new AiGeneratedFollowUpAnalysis(understandingScore, feedback);
    }

    public AiLearnerProfileAssessment assessLearnerProfile(Map<String, Object> input, TeamLearningStyle fallbackStyle) {
        Map<String, Object> request = new HashMap<>();
        request.put("task", "learner_profile");
        request.put("input", input);
        request.put("fallbackStyle", fallbackStyle.name());
        JsonNode json = requestJson("prompts/ai-team-building-system.txt", request);
        if (json == null) {
            return null;
        }
        String styleValue = json.path("learningStyle").asText(fallbackStyle.name()).trim().toUpperCase();
        String summary = json.path("profileSummary").asText("").trim();
        TeamLearningStyle style = parseStyle(styleValue, fallbackStyle);
        if (summary.isBlank()) {
            return null;
        }
        return new AiLearnerProfileAssessment(style, summary);
    }

    public AiTeamMatchingAssessment assessTeamMatching(
            List<Map<String, Object>> profiles,
            int fallbackTeamBuildingScore,
            int fallbackDiversityScore,
            String fallbackSummary
    ) {
        Map<String, Object> request = new HashMap<>();
        request.put("task", "team_matching");
        request.put("profiles", profiles);
        request.put("fallbackTeamBuildingScore", fallbackTeamBuildingScore);
        request.put("fallbackDiversityScore", fallbackDiversityScore);
        request.put("fallbackSummary", fallbackSummary);
        JsonNode json = requestJson("prompts/ai-team-building-system.txt", request);
        if (json == null) {
            return null;
        }
        String summary = json.path("matchingSummary").asText("").trim();
        if (summary.isBlank()) {
            return null;
        }
        int teamBuildingScore = clampOrFallback(json.path("teamBuildingScore").asInt(-1), fallbackTeamBuildingScore);
        int diversityScore = clampOrFallback(json.path("profileDiversityScore").asInt(-1), fallbackDiversityScore);
        return new AiTeamMatchingAssessment(teamBuildingScore, diversityScore, summary);
    }

    private JsonNode requestJson(String promptPath, Object input) {
        if (!isConfigured()) {
            return null;
        }
        try {
            String model = normalizeModelId(properties.model());
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
            request.put("model", model);
            request.put("temperature", properties.temperature());
            request.put("response_format", Map.of("type", "json_object"));
            request.put("messages", List.of(
                    Map.of("role", "system", "content", loadPrompt(promptPath)),
                    Map.of("role", "user", "content", objectMapper.writeValueAsString(input))
            ));

            String responseBody = client.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(String.class);
            if (responseBody == null || responseBody.isBlank()) {
                return null;
            }
            JsonNode response = objectMapper.readTree(responseBody);
            String content = response.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                return null;
            }
            return objectMapper.readTree(content);
        } catch (Exception ex) {
            log.warn("AI feature request failed, falling back to deterministic logic", ex);
            return null;
        }
    }

    private String normalizeModelId(String rawModel) {
        return rawModel == null ? "" : rawModel.trim().toLowerCase().replace(' ', '-');
    }

    private String loadPrompt(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }

    private FollowUpDifficultyLevel parseDifficulty(String value, FollowUpDifficultyLevel fallback) {
        try {
            return FollowUpDifficultyLevel.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private TeamLearningStyle parseStyle(String value, TeamLearningStyle fallback) {
        try {
            return TeamLearningStyle.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private int clamp(int score) {
        if (score < 0) {
            return -1;
        }
        return Math.max(0, Math.min(100, score));
    }

    private int clampOrFallback(int value, int fallback) {
        int clamped = clamp(value);
        return clamped < 0 ? fallback : clamped;
    }

    public record AiGeneratedFollowUpQuestion(
            String questionText,
            FollowUpDifficultyLevel difficultyLevel
    ) {
    }

    public record AiGeneratedFollowUpAnalysis(
            int understandingScore,
            String feedback
    ) {
    }

    public record AiLearnerProfileAssessment(
            TeamLearningStyle learningStyle,
            String profileSummary
    ) {
    }

    public record AiTeamMatchingAssessment(
            int teamBuildingScore,
            int profileDiversityScore,
            String matchingSummary
    ) {
    }
}
