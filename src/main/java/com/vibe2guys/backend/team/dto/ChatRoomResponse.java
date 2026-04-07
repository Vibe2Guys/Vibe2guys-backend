package com.vibe2guys.backend.team.dto;

import com.vibe2guys.backend.team.domain.TeamChatRoom;

public record ChatRoomResponse(
        Long chatRoomId,
        Long teamId
) {
    public static ChatRoomResponse from(TeamChatRoom chatRoom) {
        return new ChatRoomResponse(chatRoom.getId(), chatRoom.getTeam().getId());
    }
}
