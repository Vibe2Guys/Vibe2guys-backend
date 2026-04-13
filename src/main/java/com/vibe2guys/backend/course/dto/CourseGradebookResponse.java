package com.vibe2guys.backend.course.dto;

import java.util.List;

public record CourseGradebookResponse(
        Long courseId,
        String courseTitle,
        int attendanceRate,
        int progressRate,
        int assignmentAverage,
        int quizAverage,
        int overallScore,
        List<CourseGradeItemResponse> assignments,
        List<CourseGradeItemResponse> quizzes
) {
}
