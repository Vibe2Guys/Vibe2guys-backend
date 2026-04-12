package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.Course;

public record CourseListItemResponse(
        Long courseId,
        String title,
        String description,
        String instructorName,
        String thumbnailUrl,
        String courseCode,
        boolean isPublic,
        boolean isEnrolled
) {
    public static CourseListItemResponse of(Course course, String instructorName, boolean isEnrolled) {
        return new CourseListItemResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                instructorName,
                course.getThumbnailUrl(),
                course.getCourseCode(),
                course.isPublic(),
                isEnrolled
        );
    }
}
