package com.vibe2guys.backend.course.repository;

import com.vibe2guys.backend.course.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByWeekIdOrderByIdAsc(Long weekId);

    List<Content> findByCourseIdOrderByIdAsc(Long courseId);
}
