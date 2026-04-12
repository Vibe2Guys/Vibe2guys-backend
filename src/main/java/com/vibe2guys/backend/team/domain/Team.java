package com.vibe2guys.backend.team.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import com.vibe2guys.backend.course.domain.Course;
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

@Getter
@Entity
@Table(name = "teams")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeamStatus status;

    @Column(name = "team_building_score", nullable = false)
    private int teamBuildingScore;

    @Column(name = "profile_diversity_score", nullable = false)
    private int profileDiversityScore;

    @Column(name = "matching_summary", length = 500)
    private String matchingSummary;

    @Builder
    private Team(
            Course course,
            String name,
            TeamStatus status,
            int teamBuildingScore,
            int profileDiversityScore,
            String matchingSummary
    ) {
        this.course = course;
        this.name = name;
        this.status = status;
        this.teamBuildingScore = teamBuildingScore;
        this.profileDiversityScore = profileDiversityScore;
        this.matchingSummary = matchingSummary;
    }

    public void reconfigured() {
        this.status = TeamStatus.RECONFIGURED;
    }

    public void updateMatching(int teamBuildingScore, int profileDiversityScore, String matchingSummary) {
        this.teamBuildingScore = teamBuildingScore;
        this.profileDiversityScore = profileDiversityScore;
        this.matchingSummary = matchingSummary;
    }
}
