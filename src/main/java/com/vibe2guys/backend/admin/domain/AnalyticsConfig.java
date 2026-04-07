package com.vibe2guys.backend.admin.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "analytics_configs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalyticsConfig extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(name = "attendance_weight", nullable = false)
    private double attendanceWeight;

    @Column(name = "progress_weight", nullable = false)
    private double progressWeight;

    @Column(name = "assignment_weight", nullable = false)
    private double assignmentWeight;

    @Column(name = "quiz_weight", nullable = false)
    private double quizWeight;

    @Column(name = "team_activity_weight", nullable = false)
    private double teamActivityWeight;

    @Column(name = "risk_threshold_high", nullable = false)
    private int riskThresholdHigh;

    @Column(name = "risk_threshold_medium", nullable = false)
    private int riskThresholdMedium;

    public AnalyticsConfig(
            Long id,
            double attendanceWeight,
            double progressWeight,
            double assignmentWeight,
            double quizWeight,
            double teamActivityWeight,
            int riskThresholdHigh,
            int riskThresholdMedium
    ) {
        this.id = id;
        this.attendanceWeight = attendanceWeight;
        this.progressWeight = progressWeight;
        this.assignmentWeight = assignmentWeight;
        this.quizWeight = quizWeight;
        this.teamActivityWeight = teamActivityWeight;
        this.riskThresholdHigh = riskThresholdHigh;
        this.riskThresholdMedium = riskThresholdMedium;
    }

    public void update(
            double attendanceWeight,
            double progressWeight,
            double assignmentWeight,
            double quizWeight,
            double teamActivityWeight,
            int riskThresholdHigh,
            int riskThresholdMedium
    ) {
        this.attendanceWeight = attendanceWeight;
        this.progressWeight = progressWeight;
        this.assignmentWeight = assignmentWeight;
        this.quizWeight = quizWeight;
        this.teamActivityWeight = teamActivityWeight;
        this.riskThresholdHigh = riskThresholdHigh;
        this.riskThresholdMedium = riskThresholdMedium;
    }
}
