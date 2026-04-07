CREATE TABLE quizzes
(
    id         BIGSERIAL PRIMARY KEY,
    course_id  BIGINT      NOT NULL REFERENCES courses (id),
    title      VARCHAR(200) NOT NULL,
    due_at     TIMESTAMPTZ NOT NULL,
    created_by BIGINT      NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quiz_questions
(
    id            BIGSERIAL PRIMARY KEY,
    quiz_id       BIGINT        NOT NULL REFERENCES quizzes (id),
    question_type VARCHAR(30)   NOT NULL,
    question_text VARCHAR(2000) NOT NULL,
    choices_json  JSONB,
    answer_key    VARCHAR(2000),
    score         INTEGER       NOT NULL,
    sort_order    INTEGER       NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quiz_submissions
(
    id              BIGSERIAL PRIMARY KEY,
    quiz_id         BIGINT      NOT NULL REFERENCES quizzes (id),
    course_id       BIGINT      NOT NULL REFERENCES courses (id),
    student_user_id BIGINT      NOT NULL REFERENCES users (id),
    objective_score INTEGER     NOT NULL,
    subjective_score INTEGER,
    total_score     INTEGER     NOT NULL,
    status          VARCHAR(30) NOT NULL,
    submitted_at    TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quiz_submission_answers
(
    id                 BIGSERIAL PRIMARY KEY,
    quiz_submission_id BIGINT        NOT NULL REFERENCES quiz_submissions (id),
    question_id        BIGINT        NOT NULL REFERENCES quiz_questions (id),
    selected_choice    VARCHAR(2000),
    answer_text        VARCHAR(10000),
    is_correct         BOOLEAN,
    awarded_score      INTEGER       NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
