package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.AssignmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateAssignmentRequest(
        @NotBlank(message = "title은 필수입니다.")
        @Size(max = 200, message = "title은 200자 이하여야 합니다.")
        String title,
        @NotBlank(message = "description은 필수입니다.")
        @Size(max = 4000, message = "description은 4000자 이하여야 합니다.")
        String description,
        @NotNull(message = "type은 필수입니다.")
        AssignmentType type,
        @NotNull(message = "dueAt은 필수입니다.")
        OffsetDateTime dueAt,
        boolean teamAssignment
) {
}
