package com.vibe2guys.backend.analytics.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.course.domain.Course;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Entity
@Table(
        name = "daily_analytics_snapshots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_analytics_student_course_date",
                columnNames = {"student_user_id", "course_id", "snapshot_date"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyAnalyticsSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "diligence_score", nullable = false)
    private int diligenceScore;

    @Column(name = "understanding_score", nullable = false)
    private int understandingScore;

    @Column(name = "engagement_score", nullable = false)
    private int engagementScore;

    @Column(name = "collaboration_score", nullable = false)
    private int collaborationScore;

    @Column(name = "dropout_risk_score", nullable = false)
    private int dropoutRiskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reasons_json", nullable = false, columnDefinition = "jsonb")
    private List<String> reasons;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_window_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> evidenceWindow;

    @Column(name = "coaching_message", nullable = false, length = 500)
    private String coachingMessage;

    @Column(name = "scoring_version", nullable = false, length = 50)
    private String scoringVersion;

    @Column(name = "computed_at", nullable = false)
    private OffsetDateTime computedAt;

    @Builder
    private DailyAnalyticsSnapshot(
            User student,
            Course course,
            LocalDate snapshotDate,
            int diligenceScore,
            int understandingScore,
            int engagementScore,
            int collaborationScore,
            int dropoutRiskScore,
            RiskLevel riskLevel,
            List<String> reasons,
            Map<String, Object> evidenceWindow,
            String coachingMessage,
            String scoringVersion,
            OffsetDateTime computedAt
    ) {
        this.student = student;
        this.course = course;
        this.snapshotDate = snapshotDate;
        this.diligenceScore = diligenceScore;
        this.understandingScore = understandingScore;
        this.engagementScore = engagementScore;
        this.collaborationScore = collaborationScore;
        this.dropoutRiskScore = dropoutRiskScore;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
        this.evidenceWindow = evidenceWindow;
        this.coachingMessage = coachingMessage;
        this.scoringVersion = scoringVersion;
        this.computedAt = computedAt;
    }

    public void update(
            int diligenceScore,
            int understandingScore,
            int engagementScore,
            int collaborationScore,
            int dropoutRiskScore,
            RiskLevel riskLevel,
            List<String> reasons,
            Map<String, Object> evidenceWindow,
            String coachingMessage,
            String scoringVersion,
            OffsetDateTime computedAt
    ) {
        this.diligenceScore = diligenceScore;
        this.understandingScore = understandingScore;
        this.engagementScore = engagementScore;
        this.collaborationScore = collaborationScore;
        this.dropoutRiskScore = dropoutRiskScore;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
        this.evidenceWindow = evidenceWindow;
        this.coachingMessage = coachingMessage;
        this.scoringVersion = scoringVersion;
        this.computedAt = computedAt;
    }
}
