package com.vibe2guys.backend.quiz.repository;

import com.vibe2guys.backend.quiz.domain.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizIdOrderBySortOrderAsc(Long quizId);
}
