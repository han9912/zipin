CREATE TABLE telegram_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    telegram_id BIGINT NOT NULL,
    username VARCHAR(64) NULL,
    display_name VARCHAR(255) NOT NULL,
    picture_url VARCHAR(2048) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_telegram_users_telegram_id (telegram_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE opportunities
    ADD COLUMN publisher_id BIGINT NULL,
    ADD INDEX idx_opportunities_publisher_id (publisher_id),
    ADD CONSTRAINT fk_opportunities_publisher
        FOREIGN KEY (publisher_id) REFERENCES telegram_users (id);
