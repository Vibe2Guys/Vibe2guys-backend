package com.vibe2guys.backend.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI vibe2guysOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vibe2guys Backend API")
                        .version("v1")
                        .description("AI learning operations platform backend API"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi learningApi() {
        return GroupedOpenApi.builder()
                .group("learning")
                .pathsToMatch("/api/v1/courses/**", "/api/v1/contents/**", "/api/v1/assignments/**", "/api/v1/quizzes/**", "/api/v1/teams/**", "/api/v1/chat-rooms/**")
                .build();
    }

    @Bean
    public GroupedOpenApi analyticsApi() {
        return GroupedOpenApi.builder()
                .group("analytics")
                .pathsToMatch("/api/v1/students/**", "/api/v1/dashboard/**", "/api/v1/reports/**", "/api/v1/instructors/**", "/api/v1/notifications/**", "/api/v1/ai/**")
                .build();
    }
}
