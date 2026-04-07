package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.analytics.domain.InstructorIntervention;

import java.time.OffsetDateTime;
import java.util.List;

public record InstructorInterventionItemResponse(
        Long interventionId,
        Long studentId,
        String studentName,
        Long instructorId,
        String instructorName,
        String type,
        String title,
        String message,
        List<String> resourceUrls,
        OffsetDateTime createdAt
) {

    public static InstructorInterventionItemResponse from(InstructorIntervention intervention) {
        return new InstructorInterventionItemResponse(
                intervention.getId(),
                intervention.getStudent().getId(),
                intervention.getStudent().getName(),
                intervention.getInstructor().getId(),
                intervention.getInstructor().getName(),
                intervention.getType().name(),
                intervention.getTitle(),
                intervention.getMessage(),
                intervention.getResourceUrls() == null ? List.of() : intervention.getResourceUrls(),
                intervention.getCreatedAt()
        );
    }
}
