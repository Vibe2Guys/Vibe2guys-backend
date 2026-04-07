package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {
}
