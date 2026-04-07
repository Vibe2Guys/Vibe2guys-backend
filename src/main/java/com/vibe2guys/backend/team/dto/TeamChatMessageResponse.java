package com.vibe2guys.backend.team.dto;

import com.vibe2guys.backend.team.domain.TeamChatMessage;

import java.time.OffsetDateTime;

public record TeamChatMessageResponse(
        Long messageId,
        Long senderUserId,
        String senderName,
        String messageBody,
        OffsetDateTime sentAt
) {
    public static TeamChatMessageResponse from(TeamChatMessage message) {
        return new TeamChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getMessageBody(),
                message.getSentAt()
        );
    }
}
