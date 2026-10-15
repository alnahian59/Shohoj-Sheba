-- Existing drafts remain unowned so the migration does not discard or expose them.
-- A citizen may claim an older draft only with its original tracking code.
CREATE TABLE app_users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE assistance_requests ADD COLUMN owner_id VARCHAR(36);
ALTER TABLE assistance_requests ADD CONSTRAINT fk_assistance_requests_owner
    FOREIGN KEY (owner_id) REFERENCES app_users(id);
CREATE INDEX idx_assistance_requests_owner_created
    ON assistance_requests(owner_id, created_at);
