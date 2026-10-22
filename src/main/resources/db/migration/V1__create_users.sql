
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       full_name VARCHAR(120) NOT NULL,
                       email VARCHAR(254) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL,
                       CHECK (role IN ('ADMIN', 'MEMBER')),
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
