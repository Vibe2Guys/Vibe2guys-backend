package com.vibe2guys.backend.assignment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeAssignmentSubmissionRequest(
        @NotNull(message = "점수는 필수입니다.")
        @Min(value = 0, message = "점수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "점수는 100 이하여야 합니다.")
        Integer score,
        @Size(max = 4000, message = "피드백은 4000자 이하여야 합니다.")
        String feedback
) {
}
