package com.vibe2guys.backend.quiz.repository;

import com.vibe2guys.backend.quiz.domain.QuizSubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionAnswerRepository extends JpaRepository<QuizSubmissionAnswer, Long> {
}
