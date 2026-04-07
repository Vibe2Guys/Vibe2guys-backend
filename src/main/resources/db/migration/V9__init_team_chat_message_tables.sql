CREATE TABLE team_chat_messages
(
    id             BIGSERIAL PRIMARY KEY,
    chat_room_id   BIGINT        NOT NULL REFERENCES team_chat_rooms (id),
    team_id        BIGINT        NOT NULL REFERENCES teams (id),
    sender_user_id BIGINT        NOT NULL REFERENCES users (id),
    message_body   VARCHAR(2000) NOT NULL,
    sent_at        TIMESTAMPTZ   NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_team_chat_messages_chat_room_sent_at ON team_chat_messages (chat_room_id, sent_at);
CREATE INDEX idx_team_chat_messages_team_sender ON team_chat_messages (team_id, sender_user_id);
