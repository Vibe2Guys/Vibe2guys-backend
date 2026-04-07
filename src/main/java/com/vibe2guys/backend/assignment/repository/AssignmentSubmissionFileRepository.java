package com.vibe2guys.backend.assignment.repository;

import com.vibe2guys.backend.assignment.domain.AssignmentSubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentSubmissionFileRepository extends JpaRepository<AssignmentSubmissionFile, Long> {
}
