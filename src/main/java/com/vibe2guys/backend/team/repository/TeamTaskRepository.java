package com.vibe2guys.backend.team.repository;

import com.vibe2guys.backend.team.domain.TeamTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamTaskRepository extends JpaRepository<TeamTask, Long> {
    List<TeamTask> findByTeamIdOrderByDueAtAscIdAsc(Long teamId);
}
