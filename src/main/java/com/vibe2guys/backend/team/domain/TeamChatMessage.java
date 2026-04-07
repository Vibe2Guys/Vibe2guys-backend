package com.vibe2guys.backend.team.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "team_chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamChatMessage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private TeamChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User sender;

    @Column(name = "message_body", nullable = false, length = 2000)
    private String messageBody;

    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;

    @Builder
    private TeamChatMessage(TeamChatRoom chatRoom, Team team, User sender, String messageBody, OffsetDateTime sentAt) {
        this.chatRoom = chatRoom;
        this.team = team;
        this.sender = sender;
        this.messageBody = messageBody;
        this.sentAt = sentAt;
    }
}
