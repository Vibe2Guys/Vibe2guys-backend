CREATE TABLE learning_events
(
    id             BIGSERIAL PRIMARY KEY,
    event_type     VARCHAR(50) NOT NULL,
    actor_user_id  BIGINT      NOT NULL REFERENCES users (id),
    course_id      BIGINT      NOT NULL REFERENCES courses (id),
    week_id        BIGINT REFERENCES course_weeks (id),
    content_id     BIGINT REFERENCES contents (id),
    resource_type  VARCHAR(50) NOT NULL,
    resource_id    BIGINT      NOT NULL,
    occurred_at    TIMESTAMPTZ NOT NULL,
    payload_json   JSONB       NOT NULL,
    schema_version INTEGER     NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_learning_events_actor_occurred_at ON learning_events (actor_user_id, occurred_at);
CREATE INDEX idx_learning_events_course_type_occurred_at ON learning_events (course_id, event_type, occurred_at);
CREATE INDEX idx_learning_events_content_type ON learning_events (content_id, event_type);

CREATE TABLE content_progress_summaries
(
    id                    BIGSERIAL PRIMARY KEY,
    course_id             BIGINT      NOT NULL REFERENCES courses (id),
    content_id            BIGINT      NOT NULL REFERENCES contents (id),
    student_user_id       BIGINT      NOT NULL REFERENCES users (id),
    watched_seconds       INTEGER     NOT NULL,
    total_seconds         INTEGER     NOT NULL,
    progress_rate         INTEGER     NOT NULL,
    last_position_seconds INTEGER     NOT NULL,
    replay_count          INTEGER     NOT NULL,
    is_completed          BOOLEAN     NOT NULL DEFAULT FALSE,
    last_event_type       VARCHAR(50),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_content_progress_content_student UNIQUE (content_id, student_user_id)
);
