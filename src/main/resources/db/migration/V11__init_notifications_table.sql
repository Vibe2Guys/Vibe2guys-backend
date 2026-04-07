CREATE TABLE notifications
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    type       VARCHAR(30)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    VARCHAR(2000) NOT NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at    TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user_read_created_at
    ON notifications (user_id, is_read, created_at DESC);
