-- =====================================================
-- SCRIPT DE CRIAÇÃO DAS TABELAS - SISTEMA SLOTIFY
-- =====================================================
-- Base de dados: PostgreSQL
-- Sistema: Slotify - Sistema de Agendamento
-- Versão: 1.0
-- =====================================================

-- Criação da tabela de estabelecimentos
CREATE TABLE establishments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    description VARCHAR(1000),
    working_hours VARCHAR(500),
    image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING')),
    category VARCHAR(100),
    cnpj VARCHAR(14),
    settings TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_establishment_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$' OR email IS NULL)
);

-- Criação da tabela de clientes
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_client_password_length CHECK (LENGTH(password) >= 6),
    CONSTRAINT chk_client_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Criação da tabela de usuários do estabelecimento
CREATE TABLE establishment_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF')),
    establishment_id BIGINT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_establishment_user_password_length CHECK (LENGTH(password) >= 6),
    CONSTRAINT chk_establishment_user_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    
    -- Foreign Keys
    CONSTRAINT fk_establishment_user_establishment 
        FOREIGN KEY (establishment_id) REFERENCES establishments(id) ON DELETE SET NULL
);

-- Criação da tabela de profissionais
CREATE TABLE professionals (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    specialties VARCHAR(500),
    establishment_id BIGINT NOT NULL,
    rating DECIMAL(3,2) DEFAULT 0.00 CHECK (rating >= 0 AND rating <= 5),
    total_appointments INTEGER DEFAULT 0 CHECK (total_appointments >= 0),
    satisfaction_rate DECIMAL(5,2) DEFAULT 0.00 CHECK (satisfaction_rate >= 0 AND satisfaction_rate <= 100),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_professional_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$' OR email IS NULL),
    
    -- Foreign Keys
    CONSTRAINT fk_professional_establishment 
        FOREIGN KEY (establishment_id) REFERENCES establishments(id) ON DELETE CASCADE
);

-- Criação da tabela de serviços
CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes >= 1),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    establishment_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    category VARCHAR(100),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_service_establishment 
        FOREIGN KEY (establishment_id) REFERENCES establishments(id) ON DELETE CASCADE
);

-- Criação da tabela de agendamentos
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    establishment_id BIGINT NOT NULL,
    appointment_datetime TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_appointment_future_date CHECK (appointment_datetime > created_at),
    
    -- Foreign Keys
    CONSTRAINT fk_appointment_client 
        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_professional 
        FOREIGN KEY (professional_id) REFERENCES professionals(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_service 
        FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_establishment 
        FOREIGN KEY (establishment_id) REFERENCES establishments(id) ON DELETE CASCADE
);

-- =====================================================
-- CRIAÇÃO DE ÍNDICES PARA PERFORMANCE
-- =====================================================

-- Índices únicos
CREATE UNIQUE INDEX idx_clients_email ON clients(email);
CREATE UNIQUE INDEX idx_establishment_users_email ON establishment_users(email);

-- Índices de performance para appointments (tabela principal)
CREATE INDEX idx_appointments_client_id ON appointments(client_id);
CREATE INDEX idx_appointments_professional_id ON appointments(professional_id);
CREATE INDEX idx_appointments_service_id ON appointments(service_id);
CREATE INDEX idx_appointments_establishment_id ON appointments(establishment_id);
CREATE INDEX idx_appointments_datetime ON appointments(appointment_datetime);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_datetime_status ON appointments(appointment_datetime, status);

-- Índices para relacionamentos
CREATE INDEX idx_professionals_establishment_id ON professionals(establishment_id);
CREATE INDEX idx_services_establishment_id ON services(establishment_id);
CREATE INDEX idx_establishment_users_establishment_id ON establishment_users(establishment_id);

-- Índices para status
CREATE INDEX idx_professionals_status ON professionals(status);
CREATE INDEX idx_establishments_status ON establishments(status);
CREATE INDEX idx_services_status ON services(status);
CREATE INDEX idx_clients_active ON clients(active);
CREATE INDEX idx_establishment_users_active ON establishment_users(active);

-- Índices compostos para queries comuns
CREATE INDEX idx_professionals_establishment_status ON professionals(establishment_id, status);
CREATE INDEX idx_services_establishment_price ON services(establishment_id, price);

-- =====================================================
-- TRIGGERS PARA ATUALIZAÇÃO AUTOMÁTICA DE TIMESTAMPS
-- =====================================================

-- Função para atualizar updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers para cada tabela
CREATE TRIGGER update_establishments_updated_at BEFORE UPDATE ON establishments 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_clients_updated_at BEFORE UPDATE ON clients 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_establishment_users_updated_at BEFORE UPDATE ON establishment_users 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_professionals_updated_at BEFORE UPDATE ON professionals 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_services_updated_at BEFORE UPDATE ON services 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_appointments_updated_at BEFORE UPDATE ON appointments 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- INSERÇÃO DE DADOS DE EXEMPLO (OPCIONAL)
-- =====================================================

-- Inserir um estabelecimento de exemplo
INSERT INTO establishments (name, email, phone, address, description, category) VALUES
('Salão Beleza & Estilo', 'contato@belezaestilo.com', '11999887766', 'Rua das Flores, 123 - Centro', 'Salão de beleza especializado em cortes modernos e tratamentos', 'Beleza');

-- Inserir um usuário admin de exemplo
INSERT INTO establishment_users (name, email, password, role, establishment_id) VALUES
('Ana Silva', 'ana@belezaestilo.com', '$2a$10$examplehashedpassword', 'ADMIN', 1);

-- Inserir um profissional de exemplo
INSERT INTO professionals (name, email, phone, specialties, establishment_id) VALUES
('Carlos Santos', 'carlos@belezaestilo.com', '11888776655', 'Corte masculino, Barba, Bigode', 1);

-- Inserir um serviço de exemplo
INSERT INTO services (name, description, duration_minutes, price, establishment_id) VALUES
('Corte Masculino', 'Corte de cabelo masculino com acabamento profissional', 30, 25.00, 1);

-- Inserir um cliente de exemplo
INSERT INTO clients (name, email, password, phone) VALUES
('João Oliveira', 'joao@email.com', '$2a$10$examplehashedpassword', '11777666555');

-- =====================================================
-- COMENTÁRIOS NAS TABELAS E COLUNAS
-- =====================================================

COMMENT ON TABLE establishments IS 'Estabelecimentos que oferecem serviços de agendamento';
COMMENT ON TABLE clients IS 'Clientes que fazem agendamentos';
COMMENT ON TABLE establishment_users IS 'Usuários administrativos dos estabelecimentos';
COMMENT ON TABLE professionals IS 'Profissionais que trabalham nos estabelecimentos';
COMMENT ON TABLE services IS 'Serviços oferecidos pelos estabelecimentos';
COMMENT ON TABLE appointments IS 'Agendamentos de serviços pelos clientes';

-- Comentários em colunas importantes
COMMENT ON COLUMN appointments.appointment_datetime IS 'Data e hora do agendamento';
COMMENT ON COLUMN professionals.rating IS 'Avaliação média do profissional (0-5)';
COMMENT ON COLUMN professionals.satisfaction_rate IS 'Taxa de satisfação em porcentagem (0-100)';
COMMENT ON COLUMN establishments.settings IS 'Configurações do estabelecimento em formato JSON';

-- =====================================================
-- FIM DO SCRIPT
-- =====================================================