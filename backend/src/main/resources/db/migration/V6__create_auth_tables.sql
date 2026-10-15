CREATE TABLE auth_refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    jti VARCHAR(80) NOT NULL UNIQUE,
    subject_type VARCHAR(20) NOT NULL CHECK (subject_type IN ('PROFESSOR', 'ALUNO')),
    subject_id BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_auth_refresh_subject ON auth_refresh_tokens(subject_type, subject_id);
CREATE INDEX idx_auth_refresh_expires ON auth_refresh_tokens(expires_at);
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    professor_id BIGINT NOT NULL REFERENCES professores(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_password_reset_professor ON password_reset_tokens(professor_id);
CREATE INDEX idx_password_reset_expires ON password_reset_tokens(expires_at);
