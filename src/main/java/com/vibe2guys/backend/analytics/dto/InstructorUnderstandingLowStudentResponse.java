package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.analytics.domain.DailyAnalyticsSnapshot;

import java.util.List;

public record InstructorUnderstandingLowStudentResponse(
        Long studentId,
        String studentName,
        int understandingScore,
        List<String> reasons
) {
    public static InstructorUnderstandingLowStudentResponse from(DailyAnalyticsSnapshot snapshot) {
        return new InstructorUnderstandingLowStudentResponse(
                snapshot.getStudent().getId(),
                snapshot.getStudent().getName(),
                snapshot.getUnderstandingScore(),
                snapshot.getReasons()
        );
    }
}
