CREATE TABLE courses
(
    id                    BIGSERIAL PRIMARY KEY,
    title                 VARCHAR(200)  NOT NULL,
    description           VARCHAR(2000) NOT NULL,
    thumbnail_url         VARCHAR(500),
    start_date            DATE          NOT NULL,
    end_date              DATE          NOT NULL,
    is_sequential_release BOOLEAN       NOT NULL DEFAULT FALSE,
    status                VARCHAR(20)   NOT NULL,
    created_by            BIGINT        NOT NULL REFERENCES users (id),
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE course_instructors
(
    id                 BIGSERIAL PRIMARY KEY,
    course_id          BIGINT      NOT NULL REFERENCES courses (id),
    instructor_user_id BIGINT      NOT NULL REFERENCES users (id),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_instructors_course_user UNIQUE (course_id, instructor_user_id)
);

CREATE TABLE course_enrollments
(
    id              BIGSERIAL PRIMARY KEY,
    course_id       BIGINT      NOT NULL REFERENCES courses (id),
    student_user_id BIGINT      NOT NULL REFERENCES users (id),
    status          VARCHAR(20) NOT NULL,
    enrolled_at     TIMESTAMPTZ NOT NULL,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_enrollments_course_student UNIQUE (course_id, student_user_id)
);

CREATE INDEX idx_course_enrollments_course_status ON course_enrollments (course_id, status);
CREATE INDEX idx_course_enrollments_student_user_id ON course_enrollments (student_user_id);

CREATE TABLE course_weeks
(
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT       NOT NULL REFERENCES courses (id),
    week_number INTEGER      NOT NULL,
    title       VARCHAR(200) NOT NULL,
    open_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_course_weeks_course_week_number UNIQUE (course_id, week_number)
);

CREATE TABLE contents
(
    id               BIGSERIAL PRIMARY KEY,
    course_id        BIGINT        NOT NULL REFERENCES courses (id),
    week_id          BIGINT REFERENCES course_weeks (id),
    type             VARCHAR(20)   NOT NULL,
    title            VARCHAR(200)  NOT NULL,
    description      VARCHAR(2000) NOT NULL,
    video_url        VARCHAR(500),
    document_url     VARCHAR(500),
    duration_seconds INTEGER,
    scheduled_at     TIMESTAMPTZ,
    open_at          TIMESTAMPTZ,
    is_published     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contents_course_week ON contents (course_id, week_id);
CREATE INDEX idx_contents_type ON contents (type);
