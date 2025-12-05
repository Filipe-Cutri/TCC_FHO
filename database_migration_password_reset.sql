-- =====================================================
-- MIGRATION: Add Password Reset Fields
-- Description: Adds reset_password_token_hash and reset_password_expiry fields to clients and establishment_users tables
-- Date: 2024-12-05
-- =====================================================

-- Add password reset fields to clients table
ALTER TABLE clients 
ADD COLUMN IF NOT EXISTS reset_password_token_hash VARCHAR(255),
ADD COLUMN IF NOT EXISTS reset_password_expiry BIGINT;

-- Add password reset fields to establishment_users table
ALTER TABLE establishment_users 
ADD COLUMN IF NOT EXISTS reset_password_token_hash VARCHAR(255),
ADD COLUMN IF NOT EXISTS reset_password_expiry BIGINT;

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_clients_reset_token ON clients(reset_password_token_hash) WHERE reset_password_token_hash IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_establishment_users_reset_token ON establishment_users(reset_password_token_hash) WHERE reset_password_token_hash IS NOT NULL;

-- Add comments
COMMENT ON COLUMN clients.reset_password_token_hash IS 'Hashed token for password reset (SHA-256)';
COMMENT ON COLUMN clients.reset_password_expiry IS 'Expiry timestamp for password reset token (milliseconds since epoch)';
COMMENT ON COLUMN establishment_users.reset_password_token_hash IS 'Hashed token for password reset (SHA-256)';
COMMENT ON COLUMN establishment_users.reset_password_expiry IS 'Expiry timestamp for password reset token (milliseconds since epoch)';

-- =====================================================
-- END OF MIGRATION
-- =====================================================
