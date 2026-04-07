package com.vibe2guys.backend.learning.repository;

import com.vibe2guys.backend.learning.domain.ContentProgressSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentProgressSummaryRepository extends JpaRepository<ContentProgressSummary, Long> {
    Optional<ContentProgressSummary> findByContentIdAndStudentId(Long contentId, Long studentId);

    List<ContentProgressSummary> findByCourseIdAndStudentId(Long courseId, Long studentId);
}
