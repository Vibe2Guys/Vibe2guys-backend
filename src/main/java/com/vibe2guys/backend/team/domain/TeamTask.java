package com.vibe2guys.backend.team.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "team_tasks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamTask extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private User assignee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TeamTaskStatus status;

    @Column(name = "due_at")
    private OffsetDateTime dueAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Builder
    private TeamTask(
            Team team,
            String title,
            String description,
            User assignee,
            TeamTaskStatus status,
            OffsetDateTime dueAt,
            User createdBy
    ) {
        this.team = team;
        this.title = title;
        this.description = description;
        this.assignee = assignee;
        this.status = status;
        this.dueAt = dueAt;
        this.createdBy = createdBy;
    }

    public void updateStatus(TeamTaskStatus status) {
        this.status = status;
    }
}
