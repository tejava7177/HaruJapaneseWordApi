CREATE TABLE notebook (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notebook_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE notebook_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notebook_id BIGINT NOT NULL,
    item_type VARCHAR(20) NOT NULL,
    word_id BIGINT NULL,
    expression VARCHAR(255) NOT NULL,
    reading VARCHAR(255) NULL,
    meaning TEXT NOT NULL,
    memo TEXT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notebook_item_notebook
        FOREIGN KEY (notebook_id) REFERENCES notebook(id) ON DELETE CASCADE,
    CONSTRAINT fk_notebook_item_word
        FOREIGN KEY (word_id) REFERENCES word(id)
);

CREATE INDEX idx_notebook_user
    ON notebook (user_id);

CREATE INDEX idx_notebook_item_notebook
    ON notebook_item (notebook_id);

CREATE UNIQUE INDEX uk_notebook_item_notebook_word
    ON notebook_item (notebook_id, word_id);
