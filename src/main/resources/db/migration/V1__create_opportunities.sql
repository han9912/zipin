CREATE TABLE opportunities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(80) NOT NULL,
    title VARCHAR(160) NOT NULL,
    content TEXT NOT NULL,
    contact VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_opportunities_created_at (created_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
