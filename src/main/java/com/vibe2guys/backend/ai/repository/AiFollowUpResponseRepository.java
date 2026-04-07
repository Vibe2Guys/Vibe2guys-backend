package com.vibe2guys.backend.ai.repository;

import com.vibe2guys.backend.ai.domain.AiFollowUpResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiFollowUpResponseRepository extends JpaRepository<AiFollowUpResponse, Long> {
    Optional<AiFollowUpResponse> findTopByQuestionIdOrderBySubmittedAtDesc(Long questionId);
}
