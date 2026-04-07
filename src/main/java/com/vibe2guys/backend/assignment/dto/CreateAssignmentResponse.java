package com.vibe2guys.backend.assignment.dto;

import com.vibe2guys.backend.assignment.domain.Assignment;

public record CreateAssignmentResponse(
        Long assignmentId,
        String title
) {
    public static CreateAssignmentResponse from(Assignment assignment) {
        return new CreateAssignmentResponse(assignment.getId(), assignment.getTitle());
    }
}
