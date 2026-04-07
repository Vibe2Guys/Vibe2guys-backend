package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);

    List<CourseEnrollment> findByStudentId(Long studentId);
}
