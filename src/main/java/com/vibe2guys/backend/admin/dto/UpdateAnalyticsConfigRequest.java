package com.vibe2guys.backend.admin.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateAnalyticsConfigRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double attendanceWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double progressWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double assignmentWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double quizWeight,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double teamActivityWeight,
        @NotNull @Min(0) @Max(100) Integer riskThresholdHigh,
        @NotNull @Min(0) @Max(100) Integer riskThresholdMedium
) {
}
