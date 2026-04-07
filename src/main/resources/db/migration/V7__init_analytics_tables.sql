CREATE TABLE daily_analytics_snapshots
(
    id                        BIGSERIAL PRIMARY KEY,
    student_user_id           BIGINT        NOT NULL REFERENCES users (id),
    course_id                 BIGINT        NOT NULL REFERENCES courses (id),
    snapshot_date             DATE          NOT NULL,
    diligence_score           INTEGER       NOT NULL,
    understanding_score       INTEGER       NOT NULL,
    engagement_score          INTEGER       NOT NULL,
    collaboration_score       INTEGER       NOT NULL,
    dropout_risk_score        INTEGER       NOT NULL,
    risk_level                VARCHAR(20)   NOT NULL,
    reasons_json              JSONB         NOT NULL,
    evidence_window_json      JSONB         NOT NULL,
    coaching_message          VARCHAR(500)  NOT NULL,
    scoring_version           VARCHAR(50)   NOT NULL,
    computed_at               TIMESTAMPTZ   NOT NULL,
    created_at                TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_daily_analytics_student_course_date UNIQUE (student_user_id, course_id, snapshot_date)
);

CREATE INDEX idx_daily_analytics_course_date ON daily_analytics_snapshots (course_id, snapshot_date);
CREATE INDEX idx_daily_analytics_student_date ON daily_analytics_snapshots (student_user_id, snapshot_date);
