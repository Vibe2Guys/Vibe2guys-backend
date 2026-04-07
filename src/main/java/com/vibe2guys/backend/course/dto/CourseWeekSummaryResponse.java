package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.CourseWeek;

public record CourseWeekSummaryResponse(
        Long weekId,
        int weekNumber,
        String title,
        boolean isOpened
) {
    public static CourseWeekSummaryResponse from(CourseWeek week) {
        boolean opened = week.getOpenAt() == null || !week.getOpenAt().isAfter(java.time.OffsetDateTime.now());
        return new CourseWeekSummaryResponse(week.getId(), week.getWeekNumber(), week.getTitle(), opened);
    }
}
