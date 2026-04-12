package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.CourseStudentMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseStudentMemoRepository extends JpaRepository<CourseStudentMemo, Long> {
    Optional<CourseStudentMemo> findByCourseIdAndStudentId(Long courseId, Long studentId);

    List<CourseStudentMemo> findByCourseIdAndStudentIdIn(Long courseId, List<Long> studentIds);
}
