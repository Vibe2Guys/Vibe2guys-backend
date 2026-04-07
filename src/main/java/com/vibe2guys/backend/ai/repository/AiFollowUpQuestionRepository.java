package com.vibe2guys.backend.ai.repository;

import com.vibe2guys.backend.ai.domain.AiFollowUpQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiFollowUpQuestionRepository extends JpaRepository<AiFollowUpQuestion, Long> {
}
