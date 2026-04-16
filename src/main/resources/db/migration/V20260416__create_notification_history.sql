CREATE TABLE notification_history (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    target_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_notification_history_user_type_target_date
        UNIQUE (user_id, notification_type, target_date),
    CONSTRAINT fk_notification_history_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_notification_history_user_type_target_date
    ON notification_history (user_id, notification_type, target_date);
