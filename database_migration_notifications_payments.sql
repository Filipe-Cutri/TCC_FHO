-- =====================================================
-- MIGRATION SCRIPT - ADD NOTIFICATIONS AND PAYMENTS
-- =====================================================
-- Add support for notifications and payments system
-- Date: 2025-10-17
-- =====================================================

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    appointment_id BIGINT,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('APPOINTMENT_REMINDER', 'APPOINTMENT_CONFIRMATION', 'APPOINTMENT_CANCELLED', 'APPOINTMENT_RESCHEDULED', 'APPOINTMENT_COMPLETED', 'PROMOTIONAL', 'SYSTEM', 'GENERAL')),
    read BOOLEAN DEFAULT false,
    sent BOOLEAN DEFAULT false,
    scheduled_for TIMESTAMP,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_notification_client 
        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_establishment 
        FOREIGN KEY (establishment_id) REFERENCES establishments(id) ON DELETE CASCADE,
    CONSTRAINT fk_notification_appointment 
        FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    appointment_id BIGINT,
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BANK_TRANSFER', 'OTHER')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'REFUNDED', 'FAILED')),
    payment_date TIMESTAMP,
    transaction_id VARCHAR(500),
    notes VARCHAR(1000),
    client_name VARCHAR(255),
    establishment_name VARCHAR(255),
    service_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_payment_client 
        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_establishment 
        FOREIGN KEY (establishment_id) REFERENCES establishments(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_appointment 
        FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL
);

-- Add selected_establishment_id column to clients if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'clients' AND column_name = 'selected_establishment_id') THEN
        ALTER TABLE clients ADD COLUMN selected_establishment_id BIGINT;
    END IF;
END $$;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_notifications_client_id ON notifications(client_id);
CREATE INDEX IF NOT EXISTS idx_notifications_establishment_id ON notifications(establishment_id);
CREATE INDEX IF NOT EXISTS idx_notifications_appointment_id ON notifications(appointment_id);
CREATE INDEX IF NOT EXISTS idx_notifications_read ON notifications(read);
CREATE INDEX IF NOT EXISTS idx_notifications_sent ON notifications(sent);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications(type);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

CREATE INDEX IF NOT EXISTS idx_payments_client_id ON payments(client_id);
CREATE INDEX IF NOT EXISTS idx_payments_establishment_id ON payments(establishment_id);
CREATE INDEX IF NOT EXISTS idx_payments_appointment_id ON payments(appointment_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_payment_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);

-- Create triggers for updated_at columns
CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments
COMMENT ON TABLE notifications IS 'Sistema de notificações para clientes sobre agendamentos e promoções';
COMMENT ON TABLE payments IS 'Registro de pagamentos realizados pelos clientes';

COMMENT ON COLUMN notifications.type IS 'Tipo da notificação: lembrete, confirmação, cancelamento, etc.';
COMMENT ON COLUMN notifications.read IS 'Indica se a notificação foi lida pelo cliente';
COMMENT ON COLUMN notifications.sent IS 'Indica se a notificação foi enviada';
COMMENT ON COLUMN payments.payment_method IS 'Método de pagamento utilizado';
COMMENT ON COLUMN payments.status IS 'Status do pagamento: pendente, concluído, cancelado, etc.';

-- =====================================================
-- END OF MIGRATION SCRIPT
-- =====================================================
