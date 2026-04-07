package com.vibe2guys.backend.quiz.repository;

import com.vibe2guys.backend.quiz.domain.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    Optional<QuizSubmission> findByQuizIdAndStudentId(Long quizId, Long studentId);
}
