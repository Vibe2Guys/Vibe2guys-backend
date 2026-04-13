package com.vibe2guys.backend.course.dto;

import java.util.List;

public record CourseInstructorGradebookResponse(
        Long courseId,
        String courseTitle,
        int studentCount,
        int averageOverallScore,
        List<CourseInstructorGradebookStudentResponse> students
) {
}
