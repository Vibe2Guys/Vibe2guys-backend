package com.vibe2guys.backend.analytics.dto;

import java.util.List;

public record InstructorStudentDetailResponse(
        Long studentId,
        String studentName,
        Long courseId,
        String courseTitle,
        StudentScoresResponse scores,
        StudentRiskResponse risk,
        StudentAiUnderstandingResponse aiUnderstanding,
        List<StudentAssignmentProgressItemResponse> assignments,
        List<StudentQuizProgressItemResponse> quizzes,
        List<StudentContentProgressItemResponse> contents,
        List<InstructorInterventionItemResponse> interventions
) {
}
