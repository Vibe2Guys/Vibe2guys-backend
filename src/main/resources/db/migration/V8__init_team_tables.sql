CREATE TABLE teams
(
    id         BIGSERIAL PRIMARY KEY,
    course_id   BIGINT       NOT NULL REFERENCES courses (id),
    name       VARCHAR(200)  NOT NULL,
    status     VARCHAR(20)   NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teams_course_id ON teams (course_id);

CREATE TABLE team_members
(
    id         BIGSERIAL PRIMARY KEY,
    team_id    BIGINT      NOT NULL REFERENCES teams (id),
    user_id    BIGINT      NOT NULL REFERENCES users (id),
    joined_at  TIMESTAMPTZ NOT NULL,
    left_at    TIMESTAMPTZ,
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_team_members_team_status ON team_members (team_id, status);
CREATE INDEX idx_team_members_user_id ON team_members (user_id);

CREATE TABLE team_chat_rooms
(
    id         BIGSERIAL PRIMARY KEY,
    team_id    BIGINT      NOT NULL UNIQUE REFERENCES teams (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
