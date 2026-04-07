package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.Course;

public record CreateCourseResponse(
        Long courseId,
        String title
) {
    public static CreateCourseResponse from(Course course) {
        return new CreateCourseResponse(course.getId(), course.getTitle());
    }
}
