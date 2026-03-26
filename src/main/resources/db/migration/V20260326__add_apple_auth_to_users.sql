-- apple_subject
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
              AND column_name = 'apple_subject'
        ),
        'SELECT 1',
        'ALTER TABLE users ADD COLUMN apple_subject VARCHAR(255) NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- auth_email
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
              AND column_name = 'auth_email'
        ),
        'SELECT 1',
        'ALTER TABLE users ADD COLUMN auth_email VARCHAR(255) NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- display_name
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
              AND column_name = 'display_name'
        ),
        'SELECT 1',
        'ALTER TABLE users ADD COLUMN display_name VARCHAR(255) NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- last_login_at
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
              AND column_name = 'last_login_at'
        ),
        'SELECT 1',
        'ALTER TABLE users ADD COLUMN last_login_at DATETIME NULL'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- unique index for apple_subject
SET @sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'users'
              AND index_name = 'uk_users_apple_subject'
        ),
        'SELECT 1',
        'ALTER TABLE users ADD UNIQUE INDEX uk_users_apple_subject (apple_subject)'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;