CREATE TABLE system_info
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    value       VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);