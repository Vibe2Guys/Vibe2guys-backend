package com.vibe2guys.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFollowUpResponseRequest(
        @NotBlank(message = "answerText는 필수입니다.")
        @Size(max = 10000, message = "answerText는 10000자 이하여야 합니다.")
        String answerText
) {
}
