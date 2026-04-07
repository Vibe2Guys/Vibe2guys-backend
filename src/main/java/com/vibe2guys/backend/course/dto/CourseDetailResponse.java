package com.vibe2guys.backend.course.dto;

import com.vibe2guys.backend.course.domain.Course;

import java.time.LocalDate;
import java.util.List;

public record CourseDetailResponse(
        Long courseId,
        String title,
        String description,
        CourseInstructorSummaryResponse instructor,
        LocalDate startDate,
        LocalDate endDate,
        List<CourseWeekSummaryResponse> weeks
) {
    public static CourseDetailResponse of(Course course, CourseInstructorSummaryResponse instructor, List<CourseWeekSummaryResponse> weeks) {
        return new CourseDetailResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                instructor,
                course.getStartDate(),
                course.getEndDate(),
                weeks
        );
    }
}
