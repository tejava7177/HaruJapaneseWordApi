CREATE TABLE review_word (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           word_id BIGINT NOT NULL,
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (id),
                           CONSTRAINT fk_review_word_user
                               FOREIGN KEY (user_id) REFERENCES users(id),
                           CONSTRAINT fk_review_word_word
                               FOREIGN KEY (word_id) REFERENCES word(id)
);

CREATE UNIQUE INDEX uk_review_word_user_word
    ON review_word (user_id, word_id);

CREATE INDEX idx_review_word_user
    ON review_word (user_id);
