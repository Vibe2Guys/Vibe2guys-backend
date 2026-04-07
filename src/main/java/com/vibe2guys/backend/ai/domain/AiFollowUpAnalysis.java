package com.vibe2guys.backend.ai.domain;

import com.vibe2guys.backend.common.persistence.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "ai_follow_up_analyses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFollowUpAnalysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private AiFollowUpQuestion question;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "response_id", nullable = false, unique = true)
    private AiFollowUpResponse response;

    @Column(name = "understanding_score", nullable = false)
    private int understandingScore;

    @Column(name = "feedback", nullable = false, length = 2000)
    private String feedback;

    @Column(name = "analyzed_at", nullable = false)
    private OffsetDateTime analyzedAt;

    @Builder
    private AiFollowUpAnalysis(
            AiFollowUpQuestion question,
            AiFollowUpResponse response,
            int understandingScore,
            String feedback,
            OffsetDateTime analyzedAt
    ) {
        this.question = question;
        this.response = response;
        this.understandingScore = understandingScore;
        this.feedback = feedback;
        this.analyzedAt = analyzedAt;
    }
}
