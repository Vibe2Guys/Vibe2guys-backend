package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.user.domain.User;

public record CourseInstructorSummaryResponse(
        Long userId,
        String name
) {
    public static CourseInstructorSummaryResponse from(User user) {
        return new CourseInstructorSummaryResponse(user.getId(), user.getName());
    }
}
