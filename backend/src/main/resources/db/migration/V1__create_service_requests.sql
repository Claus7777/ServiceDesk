CREATE TABLE service_requests (
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL CHECK (length(trim(title)) > 0),
    description VARCHAR(4000) NOT NULL CHECK (length(trim(description)) > 0),
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('NEW', 'IN_PROGRESS', 'RESOLVED')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_requests_created ON service_requests (created_at DESC, id DESC);
CREATE INDEX idx_requests_status_created ON service_requests (status, created_at DESC, id DESC);
