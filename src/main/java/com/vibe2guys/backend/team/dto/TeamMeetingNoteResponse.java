package com.vibe2guys.backend.team.dto;

import com.vibe2guys.backend.team.domain.TeamMeetingNote;

import java.time.OffsetDateTime;

public record TeamMeetingNoteResponse(
        Long noteId,
        String title,
        String noteBody,
        String createdByName,
        OffsetDateTime createdAt
) {
    public static TeamMeetingNoteResponse from(TeamMeetingNote note) {
        return new TeamMeetingNoteResponse(
                note.getId(),
                note.getTitle(),
                note.getNoteBody(),
                note.getCreatedBy().getName(),
                note.getCreatedAt()
        );
    }
}
