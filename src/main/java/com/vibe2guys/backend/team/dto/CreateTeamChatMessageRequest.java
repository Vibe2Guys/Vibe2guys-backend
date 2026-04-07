package com.vibe2guys.backend.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamChatMessageRequest(
        @NotBlank(message = "messageBody는 필수입니다.")
        @Size(max = 2000, message = "messageBody는 2000자 이하여야 합니다.")
        String messageBody
) {
}
