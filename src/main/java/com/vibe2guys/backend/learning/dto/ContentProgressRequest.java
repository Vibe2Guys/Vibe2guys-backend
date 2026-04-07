package com.vibe2guys.backend.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ContentProgressRequest(
        @Min(value = 0, message = "watchedSeconds는 0 이상이어야 합니다.")
        int watchedSeconds,
        @Min(value = 1, message = "totalSeconds는 1 이상이어야 합니다.")
        int totalSeconds,
        @Min(value = 0, message = "lastPositionSeconds는 0 이상이어야 합니다.")
        int lastPositionSeconds,
        @Min(value = 0, message = "replayCount는 0 이상이어야 합니다.")
        int replayCount,
        Integer stoppedSegmentStart,
        Integer stoppedSegmentEnd,
        @NotNull(message = "eventType은 필수입니다.")
        ContentProgressEventType eventType
) {
}
