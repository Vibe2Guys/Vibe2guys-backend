package com.vibe2guys.backend.learning.repository;

import com.vibe2guys.backend.learning.domain.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, Long> {
    Optional<AttendanceSummary> findByContentIdAndStudentId(Long contentId, Long studentId);
}
