package com.vibe2guys.backend.quiz.repository;

import com.vibe2guys.backend.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourseIdOrderByDueAtAsc(Long courseId);
}
