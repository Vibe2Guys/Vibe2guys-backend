package com.vibe2guys.backend.assignment.repository;

import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}
