package com.vibe2guys.backend.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamMeetingNoteRequest(
        @NotBlank(message = "회의 제목은 필수입니다.")
        @Size(max = 200, message = "회의 제목은 200자 이하여야 합니다.")
        String title,
        @NotBlank(message = "회의 메모는 필수입니다.")
        @Size(max = 5000, message = "회의 메모는 5000자 이하여야 합니다.")
        String noteBody
) {
}
