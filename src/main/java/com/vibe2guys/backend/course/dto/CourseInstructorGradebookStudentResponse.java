package com.vibe2guys.backend.course.dto;

public record CourseInstructorGradebookStudentResponse(
        Long studentId,
        String studentName,
        String email,
        int progressRate,
        int attendanceRate,
        int assignmentAverage,
        int quizAverage,
        int overallScore,
        String riskLevel,
        String statusSummary
) {
}
