CREATE TABLE team_tasks
(
    id                 BIGSERIAL PRIMARY KEY,
    team_id            BIGINT        NOT NULL REFERENCES teams (id),
    title              VARCHAR(200)  NOT NULL,
    description        VARCHAR(2000),
    assignee_user_id   BIGINT REFERENCES users (id),
    status             VARCHAR(30)   NOT NULL,
    due_at             TIMESTAMPTZ,
    created_by         BIGINT        NOT NULL REFERENCES users (id),
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_team_tasks_team_status ON team_tasks (team_id, status, due_at);

CREATE TABLE team_meeting_notes
(
    id         BIGSERIAL PRIMARY KEY,
    team_id     BIGINT        NOT NULL REFERENCES teams (id),
    title       VARCHAR(200)  NOT NULL,
    note_body   VARCHAR(5000) NOT NULL,
    created_by  BIGINT        NOT NULL REFERENCES users (id),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_team_meeting_notes_team_created ON team_meeting_notes (team_id, created_at DESC);
