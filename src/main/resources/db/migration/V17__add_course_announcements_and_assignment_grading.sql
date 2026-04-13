CREATE TABLE course_announcements
(
    id         BIGSERIAL PRIMARY KEY,
    course_id  BIGINT        NOT NULL REFERENCES courses (id),
    title      VARCHAR(200)  NOT NULL,
    body       VARCHAR(4000) NOT NULL,
    is_pinned  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_by BIGINT        NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_course_announcements_course_created
    ON course_announcements (course_id, is_pinned DESC, created_at DESC);

ALTER TABLE assignments
    ADD COLUMN max_score INTEGER NOT NULL DEFAULT 100;

ALTER TABLE assignment_submissions
    ADD COLUMN score INTEGER,
    ADD COLUMN feedback_text VARCHAR(4000),
    ADD COLUMN graded_by BIGINT REFERENCES users (id),
    ADD COLUMN graded_at TIMESTAMPTZ;
