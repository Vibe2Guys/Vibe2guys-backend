package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.Course;

public record CourseListItemResponse(
        Long courseId,
        String title,
        String instructorName,
        boolean isEnrolled
) {
    public static CourseListItemResponse of(Course course, String instructorName, boolean isEnrolled) {
        return new CourseListItemResponse(course.getId(), course.getTitle(), instructorName, isEnrolled);
    }
}
