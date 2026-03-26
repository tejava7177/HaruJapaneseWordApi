CREATE TABLE IF NOT EXISTS user_device_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    device_token VARCHAR(512) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    push_enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_seen_at DATETIME NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_device_token_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_device_token_device_token
    ON user_device_token (device_token);

CREATE INDEX IF NOT EXISTS idx_user_device_token_user_push_enabled
    ON user_device_token (user_id, push_enabled);
