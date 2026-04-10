package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.CourseInstructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseInstructorRepository extends JpaRepository<CourseInstructor, Long> {
    List<CourseInstructor> findByInstructorId(Long instructorId);

    List<CourseInstructor> findByCourseId(Long courseId);

    List<CourseInstructor> findByCourseIdIn(List<Long> courseIds);

    boolean existsByInstructorIdAndCourseId(Long instructorId, Long courseId);
}
