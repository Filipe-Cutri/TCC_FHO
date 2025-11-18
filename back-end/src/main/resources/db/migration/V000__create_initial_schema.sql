-- V000__create_initial_schema.sql
-- Initial database schema for Slotfy application
-- PostgreSQL database

-- ==============================================
-- Table: clients
-- Description: Stores client user information
-- ==============================================
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN DEFAULT true,
    selected_establishment_id BIGINT,
    reset_password_token_hash VARCHAR(255),
    reset_password_expiry BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- ==============================================
-- Table: establishments
-- Description: Stores establishment information
-- ==============================================
CREATE TABLE establishments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    description VARCHAR(1000),
    working_hours VARCHAR(500),
    image_url VARCHAR(500),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    category VARCHAR(100),
    cnpj VARCHAR(14),
    settings TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- ==============================================
-- Table: establishment_users
-- Description: Stores establishment admin/staff users
-- ==============================================
CREATE TABLE establishment_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    establishment_id BIGINT,
    active BOOLEAN DEFAULT true,
    reset_password_token_hash VARCHAR(255),
    reset_password_expiry BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- ==============================================
-- Table: professionals
-- Description: Stores professional information
-- ==============================================
CREATE TABLE professionals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    specialties VARCHAR(500),
    establishment_id BIGINT NOT NULL,
    rating DECIMAL(3, 2) DEFAULT 0,
    total_appointments INTEGER DEFAULT 0,
    satisfaction_rate DECIMAL(5, 2) DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- ==============================================
-- Table: services
-- Description: Stores services offered by establishments
-- ==============================================
CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    duration_minutes INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    establishment_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    category VARCHAR(100),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- ==============================================
-- Table: appointments
-- Description: Stores appointment/booking information
-- ==============================================
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    appointment_datetime TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'SCHEDULED',
    notes VARCHAR(1000),
    client_name VARCHAR(255),
    professional_name VARCHAR(255),
    service_name VARCHAR(255),
    service_duration_minutes INTEGER,
    service_price DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- ==============================================
-- Table: notifications
-- Description: Stores notification/reminder information
-- ==============================================
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    appointment_id BIGINT,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL,
    read BOOLEAN DEFAULT false,
    sent BOOLEAN DEFAULT false,
    scheduled_for TIMESTAMP,
    sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- ==============================================
-- Table: payments
-- Description: Stores payment transaction information
-- ==============================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    appointment_id BIGINT,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_date TIMESTAMP,
    transaction_id VARCHAR(500),
    notes VARCHAR(1000),
    client_name VARCHAR(255),
    establishment_name VARCHAR(255),
    service_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- ==============================================
-- Foreign Key Constraints
-- ==============================================

-- establishment_users foreign keys
ALTER TABLE establishment_users 
    ADD CONSTRAINT fk_establishment_users_establishment 
    FOREIGN KEY (establishment_id) REFERENCES establishments(id);

-- professionals foreign keys
ALTER TABLE professionals 
    ADD CONSTRAINT fk_professionals_establishment 
    FOREIGN KEY (establishment_id) REFERENCES establishments(id);

-- services foreign keys
ALTER TABLE services 
    ADD CONSTRAINT fk_services_establishment 
    FOREIGN KEY (establishment_id) REFERENCES establishments(id);

-- appointments foreign keys
ALTER TABLE appointments 
    ADD CONSTRAINT fk_appointments_client 
    FOREIGN KEY (client_id) REFERENCES clients(id);

ALTER TABLE appointments 
    ADD CONSTRAINT fk_appointments_professional 
    FOREIGN KEY (professional_id) REFERENCES professionals(id);

ALTER TABLE appointments 
    ADD CONSTRAINT fk_appointments_service 
    FOREIGN KEY (service_id) REFERENCES services(id);

ALTER TABLE appointments 
    ADD CONSTRAINT fk_appointments_establishment 
    FOREIGN KEY (establishment_id) REFERENCES establishments(id);

-- notifications foreign keys
ALTER TABLE notifications 
    ADD CONSTRAINT fk_notifications_client 
    FOREIGN KEY (client_id) REFERENCES clients(id);

ALTER TABLE notifications 
    ADD CONSTRAINT fk_notifications_establishment 
    FOREIGN KEY (establishment_id) REFERENCES establishments(id);

ALTER TABLE notifications 
    ADD CONSTRAINT fk_notifications_appointment 
    FOREIGN KEY (appointment_id) REFERENCES appointments(id);

-- payments foreign keys
ALTER TABLE payments 
    ADD CONSTRAINT fk_payments_client 
    FOREIGN KEY (client_id) REFERENCES clients(id);

ALTER TABLE payments 
    ADD CONSTRAINT fk_payments_establishment 
    FOREIGN KEY (establishment_id) REFERENCES establishments(id);

ALTER TABLE payments 
    ADD CONSTRAINT fk_payments_appointment 
    FOREIGN KEY (appointment_id) REFERENCES appointments(id);

-- ==============================================
-- Indexes for Performance
-- ==============================================

-- Indexes on email columns (for login/search)
CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_establishment_users_email ON establishment_users(email);

-- Indexes on password reset tokens (for lookup during password reset)
CREATE INDEX idx_clients_reset_token_hash ON clients(reset_password_token_hash);
CREATE INDEX idx_establishment_users_reset_token_hash ON establishment_users(reset_password_token_hash);

-- Indexes on foreign keys for better join performance
CREATE INDEX idx_establishment_users_establishment_id ON establishment_users(establishment_id);
CREATE INDEX idx_professionals_establishment_id ON professionals(establishment_id);
CREATE INDEX idx_services_establishment_id ON services(establishment_id);
CREATE INDEX idx_appointments_client_id ON appointments(client_id);
CREATE INDEX idx_appointments_professional_id ON appointments(professional_id);
CREATE INDEX idx_appointments_service_id ON appointments(service_id);
CREATE INDEX idx_appointments_establishment_id ON appointments(establishment_id);
CREATE INDEX idx_appointments_datetime ON appointments(appointment_datetime);
CREATE INDEX idx_notifications_client_id ON notifications(client_id);
CREATE INDEX idx_notifications_establishment_id ON notifications(establishment_id);
CREATE INDEX idx_notifications_appointment_id ON notifications(appointment_id);
CREATE INDEX idx_payments_client_id ON payments(client_id);
CREATE INDEX idx_payments_establishment_id ON payments(establishment_id);
CREATE INDEX idx_payments_appointment_id ON payments(appointment_id);

-- Index on status columns for filtering
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_notifications_read ON notifications(read);
CREATE INDEX idx_notifications_sent ON notifications(sent);
CREATE INDEX idx_payments_status ON payments(status);

-- Composite index for common queries
CREATE INDEX idx_clients_active_email ON clients(active, email);
CREATE INDEX idx_establishment_users_active_email ON establishment_users(active, email);
