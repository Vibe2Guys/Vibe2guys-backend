package com.vibe2guys.backend.team.repository;

import com.vibe2guys.backend.team.domain.TeamMeetingNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMeetingNoteRepository extends JpaRepository<TeamMeetingNote, Long> {
    List<TeamMeetingNote> findByTeamIdOrderByCreatedAtDesc(Long teamId);
}
