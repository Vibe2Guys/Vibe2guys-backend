CREATE TABLE analytics_configs
(
    id                    BIGINT PRIMARY KEY,
    attendance_weight     DOUBLE PRECISION NOT NULL,
    progress_weight       DOUBLE PRECISION NOT NULL,
    assignment_weight     DOUBLE PRECISION NOT NULL,
    quiz_weight           DOUBLE PRECISION NOT NULL,
    team_activity_weight  DOUBLE PRECISION NOT NULL,
    risk_threshold_high   INTEGER NOT NULL,
    risk_threshold_medium INTEGER NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO analytics_configs (
    id,
    attendance_weight,
    progress_weight,
    assignment_weight,
    quiz_weight,
    team_activity_weight,
    risk_threshold_high,
    risk_threshold_medium
) VALUES (
    1,
    0.2,
    0.2,
    0.2,
    0.2,
    0.2,
    75,
    50
);
