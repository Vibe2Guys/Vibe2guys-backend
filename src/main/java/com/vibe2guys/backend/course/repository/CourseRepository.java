package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.Course;
import com.vibe2guys.backend.course.domain.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatus(CourseStatus status);

    Page<Course> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Course> findByIsPublicTrue(Pageable pageable);

    Page<Course> findByIsPublicTrueAndTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Optional<Course> findByCourseCodeIgnoreCase(String courseCode);
}
