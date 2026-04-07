package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.analytics.domain.InterventionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateInstructorInterventionRequest(
        @NotNull Long studentId,
        @NotNull InterventionType type,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 4000) String message,
        @Size(max = 10) List<@Size(max = 500) String> resourceUrls
) {
}
