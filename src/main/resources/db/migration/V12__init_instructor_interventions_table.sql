CREATE TABLE instructor_interventions
(
    id                 BIGSERIAL PRIMARY KEY,
    course_id          BIGINT       NOT NULL REFERENCES courses (id),
    student_user_id    BIGINT       NOT NULL REFERENCES users (id),
    instructor_user_id BIGINT       NOT NULL REFERENCES users (id),
    type               VARCHAR(40)  NOT NULL,
    title              VARCHAR(200) NOT NULL,
    message            VARCHAR(4000) NOT NULL,
    resource_urls_json JSONB,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_instructor_interventions_course_created_at
    ON instructor_interventions (course_id, created_at DESC);

CREATE INDEX idx_instructor_interventions_course_student
    ON instructor_interventions (course_id, student_user_id);
