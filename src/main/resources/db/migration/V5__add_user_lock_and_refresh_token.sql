ALTER TABLE `user`
    ADD COLUMN account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN last_login_at DATETIME NULL;

CREATE TABLE refresh_token (
    id CHAR(36) NOT NULL,
    token VARCHAR(255) NOT NULL,
    user_id CHAR(36) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    created_by VARCHAR(191) NULL,
    updated_by VARCHAR(191) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token_token (token),
    KEY idx_refresh_token_user_id (user_id),
    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
        REFERENCES `user` (id)
) ENGINE=InnoDB;
