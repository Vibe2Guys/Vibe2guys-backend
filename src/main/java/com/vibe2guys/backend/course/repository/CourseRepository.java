package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatus(CourseStatus status);

    Page<Course> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
