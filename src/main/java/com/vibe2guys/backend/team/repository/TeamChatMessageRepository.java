package com.vibe2guys.backend.team.repository;

import com.vibe2guys.backend.team.domain.TeamChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamChatMessageRepository extends JpaRepository<TeamChatMessage, Long> {
    List<TeamChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    List<TeamChatMessage> findByTeamIdOrderBySentAtAsc(Long teamId);
}
