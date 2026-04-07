CREATE TABLE ai_follow_up_questions
(
    id               BIGSERIAL PRIMARY KEY,
    course_id        BIGINT        NOT NULL REFERENCES courses (id),
    content_id       BIGINT REFERENCES contents (id),
    student_user_id  BIGINT        NOT NULL REFERENCES users (id),
    context_type     VARCHAR(30)   NOT NULL,
    source_text      VARCHAR(10000) NOT NULL,
    question_text    VARCHAR(2000) NOT NULL,
    difficulty_level VARCHAR(20)   NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_follow_up_questions_student_course ON ai_follow_up_questions (student_user_id, course_id);

CREATE TABLE ai_follow_up_responses
(
    id                     BIGSERIAL PRIMARY KEY,
    question_id            BIGINT        NOT NULL UNIQUE REFERENCES ai_follow_up_questions (id),
    student_user_id        BIGINT        NOT NULL REFERENCES users (id),
    answer_text            VARCHAR(10000) NOT NULL,
    response_delay_seconds INTEGER       NOT NULL,
    submitted_at           TIMESTAMPTZ   NOT NULL,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_follow_up_responses_question ON ai_follow_up_responses (question_id);

CREATE TABLE ai_follow_up_analyses
(
    id                  BIGSERIAL PRIMARY KEY,
    question_id         BIGINT        NOT NULL UNIQUE REFERENCES ai_follow_up_questions (id),
    response_id         BIGINT        NOT NULL UNIQUE REFERENCES ai_follow_up_responses (id),
    understanding_score INTEGER       NOT NULL,
    feedback            VARCHAR(2000) NOT NULL,
    analyzed_at         TIMESTAMPTZ   NOT NULL,
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
