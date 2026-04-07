package com.vibe2guys.backend.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ContentProgressRequest(
        @Min(value = 0, message = "watchedSeconds는 0 이상이어야 합니다.")
        int watchedSeconds,
        @Min(value = 1, message = "totalSeconds는 1 이상이어야 합니다.")
        int totalSeconds,
        @Min(value = 0, message = "progressRate는 0 이상이어야 합니다.")
        @Max(value = 100, message = "progressRate는 100 이하여야 합니다.")
        int progressRate,
        @Min(value = 0, message = "lastPositionSeconds는 0 이상이어야 합니다.")
        int lastPositionSeconds,
        @Min(value = 0, message = "replayCount는 0 이상이어야 합니다.")
        int replayCount,
        Integer stoppedSegmentStart,
        Integer stoppedSegmentEnd,
        @NotBlank(message = "eventType은 필수입니다.")
        String eventType
) {
}
