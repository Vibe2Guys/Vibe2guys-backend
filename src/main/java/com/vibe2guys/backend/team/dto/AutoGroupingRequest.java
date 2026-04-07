package com.vibe2guys.backend.team.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AutoGroupingRequest(
        @Min(value = 2, message = "teamSize는 2 이상이어야 합니다.")
        @Max(value = 10, message = "teamSize는 10 이하여야 합니다.")
        Integer teamSize
) {
}
