-- ============================================================
-- Authentication Service - Initial Database Schema
-- PostgreSQL
-- Java 17 + Spring Boot
--
-- Supports:
--   - Google OIDC
--   - Apple OIDC
--   - Mobile OTP
--   - JWT / Refresh Tokens
--   - Session Management
--   - RBAC
--   - Audit Logging
-- ============================================================


-- ============================================================
-- 1. EXTENSIONS
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- 2. USERS
-- ============================================================

CREATE TABLE users (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    email               VARCHAR(255),
    phone               VARCHAR(20),

    first_name          VARCHAR(100),
    last_name           VARCHAR(100),

    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',

    email_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    phone_verified      BOOLEAN         NOT NULL DEFAULT FALSE,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at       TIMESTAMPTZ,

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DELETED'))
);


CREATE UNIQUE INDEX uq_users_email
    ON users (LOWER(email))
    WHERE email IS NOT NULL;

CREATE UNIQUE INDEX uq_users_phone
    ON users (phone)
    WHERE phone IS NOT NULL;

CREATE INDEX idx_users_status
    ON users (status);


-- ============================================================
-- 3. USER IDENTITIES
--
-- Stores external authentication identities:
--   GOOGLE
--   APPLE
--
-- provider_user_id should contain the provider's stable "sub".
-- ============================================================

CREATE TABLE user_identities (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID            NOT NULL,
    provider            VARCHAR(20)     NOT NULL,
    provider_user_id    VARCHAR(255)    NOT NULL,

    email               VARCHAR(255),
    display_name        VARCHAR(255),
    profile_picture     VARCHAR(500),

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_identities_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_user_identity_provider
        CHECK (provider IN ('GOOGLE', 'APPLE'))
);


CREATE UNIQUE INDEX uq_user_identity_provider
    ON user_identities (provider, provider_user_id);

CREATE INDEX idx_user_identities_user_id
    ON user_identities (user_id);


-- ============================================================
-- 4. OTP CHALLENGES
--
-- Stores hashed OTP values.
-- NEVER store the raw OTP.
-- ============================================================

CREATE TABLE otp_challenges (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID,
    phone_number        VARCHAR(20)     NOT NULL,

    otp_hash            VARCHAR(255)    NOT NULL,

    purpose             VARCHAR(50)     NOT NULL,

    expires_at          TIMESTAMPTZ     NOT NULL,

    attempts            INTEGER         NOT NULL DEFAULT 0,

    verified_at         TIMESTAMPTZ,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_otp_challenges_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_otp_attempts
        CHECK (attempts >= 0),

    CONSTRAINT chk_otp_purpose
        CHECK (
            purpose IN (
                'LOGIN',
                'PHONE_VERIFICATION',
                'PHONE_CHANGE'
            )
        )
);


CREATE INDEX idx_otp_challenges_phone
    ON otp_challenges (phone_number);

CREATE INDEX idx_otp_challenges_user
    ON otp_challenges (user_id);

CREATE INDEX idx_otp_challenges_expiry
    ON otp_challenges (expires_at);

CREATE INDEX idx_otp_challenges_active
    ON otp_challenges (phone_number, purpose, expires_at)
    WHERE verified_at IS NULL;


-- ============================================================
-- 5. ROLES
-- ============================================================

CREATE TABLE roles (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    name                VARCHAR(50)     NOT NULL,
    description         VARCHAR(255),

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE UNIQUE INDEX uq_roles_name
    ON roles (LOWER(name));


-- ============================================================
-- 6. USER ROLES
--
-- Many-to-many:
--
-- users <----> roles
-- ============================================================

CREATE TABLE user_roles (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID            NOT NULL,
    role_id             UUID            NOT NULL,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_user_roles
        UNIQUE (user_id, role_id)
);


CREATE INDEX idx_user_roles_user
    ON user_roles (user_id);

CREATE INDEX idx_user_roles_role
    ON user_roles (role_id);


-- ============================================================
-- 7. PERMISSIONS
-- ============================================================

CREATE TABLE permissions (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    name                VARCHAR(100)    NOT NULL,
    description         VARCHAR(255),

    resource            VARCHAR(100)    NOT NULL,
    action              VARCHAR(100)    NOT NULL,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_permission_resource_action
        UNIQUE (resource, action)
);


CREATE UNIQUE INDEX uq_permissions_name
    ON permissions (LOWER(name));

CREATE INDEX idx_permissions_resource_action
    ON permissions (resource, action);


-- ============================================================
-- 8. ROLE PERMISSIONS
--
-- Many-to-many:
--
-- roles <----> permissions
-- ============================================================

CREATE TABLE role_permissions (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    role_id             UUID            NOT NULL,
    permission_id       UUID            NOT NULL,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_role_permissions
        UNIQUE (role_id, permission_id)
);


CREATE INDEX idx_role_permissions_role
    ON role_permissions (role_id);

CREATE INDEX idx_role_permissions_permission
    ON role_permissions (permission_id);


-- ============================================================
-- 9. REFRESH TOKENS
--
-- NEVER store the raw refresh token.
-- Store only its hash.
-- ============================================================

CREATE TABLE refresh_tokens (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID            NOT NULL,

    token_hash          VARCHAR(255)    NOT NULL,

    device_id           VARCHAR(100)    NOT NULL,

    expires_at          TIMESTAMPTZ     NOT NULL,

    revoked_at          TIMESTAMPTZ,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);


CREATE UNIQUE INDEX uq_refresh_tokens_hash
    ON refresh_tokens (token_hash);

CREATE INDEX idx_refresh_tokens_user
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_device
    ON refresh_tokens (user_id, device_id);

CREATE INDEX idx_refresh_tokens_expiry
    ON refresh_tokens (expires_at);

CREATE INDEX idx_refresh_tokens_active
    ON refresh_tokens (user_id, device_id)
    WHERE revoked_at IS NULL;


-- ============================================================
-- 10. USER SESSIONS
-- ============================================================

CREATE TABLE user_sessions (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID            NOT NULL,

    refresh_token_id    UUID,

    device_id           VARCHAR(100)    NOT NULL,

    ip_address          INET,

    user_agent          VARCHAR(500),

    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,

    last_activity_at    TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_sessions_refresh_token
        FOREIGN KEY (refresh_token_id)
        REFERENCES refresh_tokens(id)
        ON DELETE SET NULL
);


CREATE INDEX idx_user_sessions_user
    ON user_sessions (user_id);

CREATE INDEX idx_user_sessions_active
    ON user_sessions (user_id, is_active);

CREATE INDEX idx_user_sessions_device
    ON user_sessions (user_id, device_id);

CREATE UNIQUE INDEX uq_user_sessions_refresh_token
    ON user_sessions (refresh_token_id)
    WHERE refresh_token_id IS NOT NULL;


-- ============================================================
-- 11. AUDIT LOGS
--
-- The ER diagram lists audit_logs but does not define its columns.
-- This is a proposed production-oriented structure.
-- ============================================================

CREATE TABLE audit_logs (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id             UUID,

    event_type          VARCHAR(100)    NOT NULL,

    provider            VARCHAR(20),

    ip_address          INET,

    user_agent          VARCHAR(500),

    resource_type       VARCHAR(100),
    resource_id         VARCHAR(255),

    success             BOOLEAN         NOT NULL DEFAULT TRUE,

    metadata            JSONB,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);


CREATE INDEX idx_audit_logs_user
    ON audit_logs (user_id);

CREATE INDEX idx_audit_logs_event_type
    ON audit_logs (event_type);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs (created_at);

CREATE INDEX idx_audit_logs_resource
    ON audit_logs (resource_type, resource_id);


-- ============================================================
-- 12. UPDATED_AT TRIGGER
--
-- Automatically updates updated_at whenever a row changes.
-- ============================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE TRIGGER trg_user_identities_updated_at
BEFORE UPDATE ON user_identities
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE TRIGGER trg_roles_updated_at
BEFORE UPDATE ON roles
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE TRIGGER trg_user_roles_updated_at
BEFORE UPDATE ON user_roles
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE TRIGGER trg_permissions_updated_at
BEFORE UPDATE ON permissions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE TRIGGER trg_role_permissions_updated_at
BEFORE UPDATE ON role_permissions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE TRIGGER trg_refresh_tokens_updated_at
BEFORE UPDATE ON refresh_tokens
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


CREATE TRIGGER trg_user_sessions_updated_at
BEFORE UPDATE ON user_sessions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();


-- ============================================================
-- 13. OPTIONAL INITIAL RBAC DATA
--
-- These are starter roles/permissions.
-- You can remove this section if you want a completely empty DB.
-- ============================================================

INSERT INTO roles (name, description)
VALUES
    ('USER', 'Standard authenticated user'),
    ('ADMIN', 'System administrator')
ON CONFLICT DO NOTHING;


INSERT INTO permissions (name, description, resource, action)
VALUES
    ('USER_READ',   'Read user information',       'USER', 'READ'),
    ('USER_UPDATE', 'Update user information',     'USER', 'UPDATE'),
    ('USER_DELETE', 'Delete user information',     'USER', 'DELETE'),
    ('ROLE_READ',   'Read role information',       'ROLE', 'READ'),
    ('ROLE_UPDATE', 'Update role information',     'ROLE', 'UPDATE'),
    ('SESSION_READ','View active sessions',        'SESSION', 'READ'),
    ('SESSION_REVOKE','Revoke active sessions',    'SESSION', 'REVOKE')
ON CONFLICT DO NOTHING;


-- ============================================================
-- END OF INITIAL AUTHENTICATION SCHEMA
-- ============================================================