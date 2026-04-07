package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.Course;

public record UpdateCourseResponse(
        Long courseId,
        String title,
        String status
) {
    public static UpdateCourseResponse from(Course course) {
        return new UpdateCourseResponse(course.getId(), course.getTitle(), course.getStatus().name());
    }
}
