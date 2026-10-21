CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    display_name VARCHAR(100) NOT NULL CHECK (length(trim(display_name)) > 0),
    password_hash VARCHAR(255) NOT NULL CHECK (length(password_hash) >= 60),
    role VARCHAR(20) NOT NULL CHECK (role IN ('REQUESTER', 'STAFF')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_accounts_email UNIQUE (email),
    CONSTRAINT ck_accounts_email_canonical CHECK (
        email = lower(trim(email)) AND length(email) > 0
    )
);
