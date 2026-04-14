-- EventMate Authentication Service Database Setup Script

-- Create database
CREATE DATABASE "eventmate-auth";

-- Connect to the database
\c eventmate-auth;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'TENANT', 'USER')),
    tenant_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Create index on tenant_id for multi-tenant queries
CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id) WHERE tenant_id IS NOT NULL;

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    token VARCHAR(500) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for refresh_tokens
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_revoked ON refresh_tokens(revoked) WHERE revoked = FALSE;

-- Insert sample admin user
-- Password: admin123 (BCrypt hashed)
INSERT INTO users (id, email, password, role, tenant_id, is_active, created_at)
VALUES (
    uuid_generate_v4(),
    'admin@eventmate.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Insert sample tenant user
-- Password: tenant123 (BCrypt hashed)
INSERT INTO users (id, email, password, role, tenant_id, is_active, created_at)
VALUES (
    uuid_generate_v4(),
    'tenant@eventmate.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'TENANT',
    uuid_generate_v4(),
    TRUE,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- Insert sample regular user
-- Password: user123 (BCrypt hashed)
INSERT INTO users (id, email, password, role, tenant_id, is_active, created_at)
VALUES (
    uuid_generate_v4(),
    'user@eventmate.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'USER',
    NULL,
    TRUE,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

-- View all users
SELECT id, email, role, tenant_id, is_active, created_at FROM users;

-- Clean up expired refresh tokens (can be run periodically)
-- DELETE FROM refresh_tokens WHERE expiry_date < CURRENT_TIMESTAMP;

-- Clean up revoked refresh tokens older than 30 days
-- DELETE FROM refresh_tokens WHERE revoked = TRUE AND created_at < CURRENT_TIMESTAMP - INTERVAL '30 days';

