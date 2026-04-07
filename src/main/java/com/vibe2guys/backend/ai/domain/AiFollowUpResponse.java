package com.vibe2guys.backend.ai.domain;

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
@Table(name = "ai_follow_up_responses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiFollowUpResponse extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private AiFollowUpQuestion question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_user_id", nullable = false)
    private User student;

    @Column(name = "answer_text", nullable = false, length = 10000)
    private String answerText;

    @Column(name = "response_delay_seconds", nullable = false)
    private int responseDelaySeconds;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Builder
    private AiFollowUpResponse(AiFollowUpQuestion question, User student, String answerText, int responseDelaySeconds, OffsetDateTime submittedAt) {
        this.question = question;
        this.student = student;
        this.answerText = answerText;
        this.responseDelaySeconds = responseDelaySeconds;
        this.submittedAt = submittedAt;
    }
}
