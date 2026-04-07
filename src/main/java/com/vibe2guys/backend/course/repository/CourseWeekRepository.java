package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.CourseWeek;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseWeekRepository extends JpaRepository<CourseWeek, Long> {
    List<CourseWeek> findByCourseIdOrderByWeekNumberAsc(Long courseId);
}
