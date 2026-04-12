package com.vibe2guys.backend.ai.dto;

import com.vibe2guys.backend.ai.domain.FollowUpContextType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateFollowUpQuestionRequest(
        @NotNull(message = "courseId는 필수입니다.")
        Long courseId,
        Long contentId,
        @NotNull(message = "studentId는 필수입니다.")
        Long studentId,
        @NotNull(message = "contextType은 필수입니다.")
        FollowUpContextType contextType,
        @Size(max = 10000, message = "sourceText는 10000자 이하여야 합니다.")
        String sourceText
) {
}
