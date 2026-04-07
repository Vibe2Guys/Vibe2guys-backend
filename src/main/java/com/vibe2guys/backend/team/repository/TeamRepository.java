package com.vibe2guys.backend.team.repository;

import com.vibe2guys.backend.team.domain.Team;
import com.vibe2guys.backend.team.domain.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByCourseIdAndStatusOrderByIdAsc(Long courseId, TeamStatus status);
}
