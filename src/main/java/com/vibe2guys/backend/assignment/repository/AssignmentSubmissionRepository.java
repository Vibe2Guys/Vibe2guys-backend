package com.vibe2guys.backend.assignment.repository;

import com.vibe2guys.backend.assignment.domain.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    Optional<AssignmentSubmission> findByIdAndAssignmentId(Long submissionId, Long assignmentId);

    List<AssignmentSubmission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);
}
