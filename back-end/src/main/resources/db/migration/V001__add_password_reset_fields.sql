-- Add password reset fields to clients table
ALTER TABLE clients ADD COLUMN IF NOT EXISTS reset_password_token_hash VARCHAR(255);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS reset_password_expiry BIGINT;

-- Add password reset fields to establishment_users table
ALTER TABLE establishment_users ADD COLUMN IF NOT EXISTS reset_password_token_hash VARCHAR(255);
ALTER TABLE establishment_users ADD COLUMN IF NOT EXISTS reset_password_expiry BIGINT;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_clients_reset_token_hash ON clients(reset_password_token_hash);
CREATE INDEX IF NOT EXISTS idx_establishment_users_reset_token_hash ON establishment_users(reset_password_token_hash);
