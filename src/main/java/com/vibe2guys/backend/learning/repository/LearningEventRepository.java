package com.vibe2guys.backend.learning.repository;

import com.vibe2guys.backend.learning.domain.LearningEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningEventRepository extends JpaRepository<LearningEvent, Long> {
}
