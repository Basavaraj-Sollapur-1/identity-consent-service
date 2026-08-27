ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_language varchar(20) NOT NULL DEFAULT 'en';
