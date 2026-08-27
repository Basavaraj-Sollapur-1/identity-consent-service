CREATE TABLE consent_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    persona_id VARCHAR(120) NOT NULL,
    version INTEGER NOT NULL,
    interactive_allowed BOOLEAN NOT NULL,
    voice_allowed BOOLEAN NOT NULL,
    avatar_allowed BOOLEAN NOT NULL,
    text_allowed BOOLEAN NOT NULL,
    withdrawn BOOLEAN NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_consent_history_user_persona ON consent_history(user_id, persona_id, changed_at);
