package com.vibe2guys.backend.assignment.repository;

import com.vibe2guys.backend.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseIdOrderByDueAtAsc(Long courseId);
}
