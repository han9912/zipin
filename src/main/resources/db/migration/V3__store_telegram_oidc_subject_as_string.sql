ALTER TABLE telegram_users
    MODIFY COLUMN telegram_id VARCHAR(255)
        CHARACTER SET ascii COLLATE ascii_bin NOT NULL;
