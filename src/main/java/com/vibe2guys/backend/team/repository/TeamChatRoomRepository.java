package com.vibe2guys.backend.team.repository;

import com.vibe2guys.backend.team.domain.TeamChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamChatRoomRepository extends JpaRepository<TeamChatRoom, Long> {
    Optional<TeamChatRoom> findByTeamId(Long teamId);
}
