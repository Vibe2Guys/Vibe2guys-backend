CREATE TABLE attendance_summaries
(
    id                 BIGSERIAL PRIMARY KEY,
    course_id          BIGINT      NOT NULL REFERENCES courses (id),
    content_id         BIGINT      NOT NULL REFERENCES contents (id),
    student_user_id    BIGINT      NOT NULL REFERENCES users (id),
    first_entered_at   TIMESTAMPTZ NOT NULL,
    last_left_at       TIMESTAMPTZ,
    attendance_minutes INTEGER     NOT NULL DEFAULT 0,
    status             VARCHAR(20) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_attendance_content_student UNIQUE (content_id, student_user_id)
);
