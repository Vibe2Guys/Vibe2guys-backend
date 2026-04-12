CREATE TABLE course_student_memos
(
    id              BIGSERIAL PRIMARY KEY,
    course_id        BIGINT       NOT NULL REFERENCES courses (id),
    student_user_id  BIGINT       NOT NULL REFERENCES users (id),
    memo_text       VARCHAR(2000),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_student_memo UNIQUE (course_id, student_user_id)
);

CREATE INDEX idx_course_student_memos_course_student
    ON course_student_memos (course_id, student_user_id);
