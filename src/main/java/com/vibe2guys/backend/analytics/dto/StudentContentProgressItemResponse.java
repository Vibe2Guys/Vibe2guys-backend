package com.vibe2guys.backend.analytics.dto;

import com.vibe2guys.backend.course.domain.Content;
import com.vibe2guys.backend.learning.domain.AttendanceSummary;
import com.vibe2guys.backend.learning.domain.ContentProgressSummary;

public record StudentContentProgressItemResponse(
        Long contentId,
        String title,
        String type,
        Integer progressRate,
        Boolean completed,
        String attendanceStatus,
        Integer attendanceMinutes
) {
    public static StudentContentProgressItemResponse of(
            Content content,
            ContentProgressSummary progress,
            AttendanceSummary attendance
    ) {
        return new StudentContentProgressItemResponse(
                content.getId(),
                content.getTitle(),
                content.getType().name(),
                progress == null ? null : progress.getProgressRate(),
                progress == null ? null : progress.isCompleted(),
                attendance == null ? null : attendance.getStatus().name(),
                attendance == null ? null : attendance.getAttendanceMinutes()
        );
    }
}
