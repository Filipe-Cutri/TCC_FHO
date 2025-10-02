-- =====================================================
-- MIGRATION: Add establishment selection to clients
-- =====================================================
-- Description: Adds selected_establishment_id field to clients table
-- to allow clients to associate with a specific establishment
-- =====================================================

-- Add the selected_establishment_id column to clients table
ALTER TABLE clients 
ADD COLUMN selected_establishment_id BIGINT;

-- Add foreign key constraint (optional but recommended)
ALTER TABLE clients
ADD CONSTRAINT fk_client_selected_establishment 
    FOREIGN KEY (selected_establishment_id) 
    REFERENCES establishments(id) 
    ON DELETE SET NULL;

-- Create index for performance
CREATE INDEX idx_clients_selected_establishment_id 
ON clients(selected_establishment_id);

-- Add comment to column
COMMENT ON COLUMN clients.selected_establishment_id IS 'ID do estabelecimento selecionado pelo cliente para agendamentos';

-- =====================================================
-- END OF MIGRATION
-- =====================================================
