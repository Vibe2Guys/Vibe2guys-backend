CREATE TABLE assignments
(
    id              BIGSERIAL PRIMARY KEY,
    course_id       BIGINT        NOT NULL REFERENCES courses (id),
    title           VARCHAR(200)  NOT NULL,
    description     VARCHAR(4000) NOT NULL,
    type            VARCHAR(30)   NOT NULL,
    team_assignment BOOLEAN       NOT NULL DEFAULT FALSE,
    due_at          TIMESTAMPTZ   NOT NULL,
    created_by      BIGINT        NOT NULL REFERENCES users (id),
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assignment_submissions
(
    id              BIGSERIAL PRIMARY KEY,
    assignment_id   BIGINT         NOT NULL REFERENCES assignments (id),
    course_id       BIGINT         NOT NULL REFERENCES courses (id),
    student_user_id BIGINT         NOT NULL REFERENCES users (id),
    answer_text     VARCHAR(10000),
    status          VARCHAR(30)    NOT NULL,
    submitted_at    TIMESTAMPTZ    NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assignment_submissions_assignment_student ON assignment_submissions (assignment_id, student_user_id);

CREATE TABLE assignment_submission_files
(
    id          BIGSERIAL PRIMARY KEY,
    submission_id BIGINT       NOT NULL REFERENCES assignment_submissions (id),
    file_url    VARCHAR(500) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);
