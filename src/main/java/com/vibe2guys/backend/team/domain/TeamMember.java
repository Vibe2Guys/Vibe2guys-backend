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
@Table(name = "team_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamMemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style", nullable = false, length = 30)
    private TeamLearningStyle learningStyle;

    @Column(name = "reliability_score", nullable = false)
    private int reliabilityScore;

    @Column(name = "initiative_score", nullable = false)
    private int initiativeScore;

    @Column(name = "support_score", nullable = false)
    private int supportScore;

    @Column(name = "understanding_score", nullable = false)
    private int understandingScore;

    @Column(name = "profile_summary", length = 300)
    private String profileSummary;

    @Builder
    private TeamMember(
            Team team,
            User user,
            OffsetDateTime joinedAt,
            OffsetDateTime leftAt,
            TeamMemberStatus status,
            TeamLearningStyle learningStyle,
            int reliabilityScore,
            int initiativeScore,
            int supportScore,
            int understandingScore,
            String profileSummary
    ) {
        this.team = team;
        this.user = user;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.status = status;
        this.learningStyle = learningStyle;
        this.reliabilityScore = reliabilityScore;
        this.initiativeScore = initiativeScore;
        this.supportScore = supportScore;
        this.understandingScore = understandingScore;
        this.profileSummary = profileSummary;
    }

    public void remove(OffsetDateTime leftAt) {
        this.leftAt = leftAt;
        this.status = TeamMemberStatus.REMOVED;
    }

    public void applyProfile(
            TeamLearningStyle learningStyle,
            int reliabilityScore,
            int initiativeScore,
            int supportScore,
            int understandingScore,
            String profileSummary
    ) {
        this.learningStyle = learningStyle;
        this.reliabilityScore = reliabilityScore;
        this.initiativeScore = initiativeScore;
        this.supportScore = supportScore;
        this.understandingScore = understandingScore;
        this.profileSummary = profileSummary;
    }
}
