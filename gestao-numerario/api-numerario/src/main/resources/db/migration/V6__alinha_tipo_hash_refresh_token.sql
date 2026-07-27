ALTER TABLE refresh_token
    MODIFY COLUMN token_hash VARCHAR(64) NOT NULL;
