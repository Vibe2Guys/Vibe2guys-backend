package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.CourseWeek;

public record CreateWeekResponse(
        Long weekId,
        int weekNumber,
        String title
) {
    public static CreateWeekResponse from(CourseWeek week) {
        return new CreateWeekResponse(week.getId(), week.getWeekNumber(), week.getTitle());
    }
}
