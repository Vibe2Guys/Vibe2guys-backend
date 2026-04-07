package com.vibe2guys.backend.learning.dto;

import com.vibe2guys.backend.learning.domain.AttendanceSummary;

public record AttendanceResponse(
        Long attendanceId,
        String status,
        Integer durationMinutes
) {
    public static AttendanceResponse started(AttendanceSummary summary) {
        return new AttendanceResponse(summary.getId(), summary.getStatus().name(), null);
    }

    public static AttendanceResponse finished(AttendanceSummary summary) {
        return new AttendanceResponse(summary.getId(), summary.getStatus().name(), summary.getAttendanceMinutes());
    }
}
