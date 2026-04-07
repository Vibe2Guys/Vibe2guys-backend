package com.vibe2guys.backend.ai.repository;

import com.vibe2guys.backend.ai.domain.AiFollowUpAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiFollowUpAnalysisRepository extends JpaRepository<AiFollowUpAnalysis, Long> {
    Optional<AiFollowUpAnalysis> findByQuestionId(Long questionId);

    List<AiFollowUpAnalysis> findByQuestionStudentIdAndQuestionCourseIdOrderByAnalyzedAtDesc(Long studentId, Long courseId);

    List<AiFollowUpAnalysis> findByQuestionStudentIdOrderByAnalyzedAtDesc(Long studentId);
}
