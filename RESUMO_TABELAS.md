# Resumo das Tabelas - Sistema Slotify

## Lista Completa das Tabelas

O sistema Slotify possui **6 tabelas principais** no banco de dados PostgreSQL:

### 1. **`clients`**
- **Propósito:** Armazenar dados dos clientes do sistema
- **Campos principais:** id, name, email, password, phone, active
- **Relacionamentos:** Relaciona com appointments (1:N)

### 2. **`establishments`** 
- **Propósito:** Dados dos estabelecimentos que oferecem serviços
- **Campos principais:** id, name, email, phone, address, description, working_hours, status, category, cnpj
- **Relacionamentos:** Relaciona com establishment_users, professionals, services e appointments (1:N para cada)

### 3. **`establishment_users`**
- **Propósito:** Usuários administrativos dos estabelecimentos (admins e funcionários)  
- **Campos principais:** id, name, email, password, role, establishment_id, active
- **Relacionamentos:** Pertence a um establishment (N:1)

### 4. **`professionals`**
- **Propósito:** Profissionais que trabalham nos estabelecimentos
- **Campos principais:** id, name, email, phone, specialties, establishment_id, rating, total_appointments, satisfaction_rate, status
- **Relacionamentos:** Pertence a um establishment (N:1), relaciona com appointments (1:N)

### 5. **`services`**
- **Propósito:** Serviços oferecidos pelos estabelecimentos
- **Campos principais:** id, name, description, duration_minutes, price, establishment_id, status, category
- **Relacionamentos:** Pertence a um establishment (N:1), relaciona com appointments (1:N)

### 6. **`appointments`**
- **Propósito:** Agendamentos realizados pelos clientes (TABELA CENTRAL)
- **Campos principais:** id, client_id, professional_id, service_id, establishment_id, appointment_datetime, status, notes
- **Relacionamentos:** Conecta clients, professionals, services e establishments (N:1 para cada)

---

## Campos Comuns (BaseEntity)

Todas as tabelas herdam os seguintes campos de auditoria:
- **`id`** (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
- **`created_at`** (TIMESTAMP, NOT NULL)
- **`updated_at`** (TIMESTAMP, NULLABLE)

---

## Enums Utilizados

- **`UserRole`:** ADMIN, STAFF
- **`EstablishmentStatus`:** ACTIVE, INACTIVE, SUSPENDED, PENDING
- **`ProfessionalStatus`:** ACTIVE, INACTIVE, SUSPENDED  
- **`ServiceStatus`:** ACTIVE, INACTIVE, SUSPENDED
- **`AppointmentStatus`:** SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW

---

## Estrutura de Relacionamentos

```
ESTABLISHMENTS (centro do sistema)
├── establishment_users (N:1)
├── professionals (N:1) 
├── services (N:1)
└── appointments (N:1)

APPOINTMENTS (tabela de conexão)
├── client_id → clients(id)
├── professional_id → professionals(id)
├── service_id → services(id)
└── establishment_id → establishments(id)
```

---

## Tecnologias

- **Banco de Dados:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate
- **Estratégia de ID:** IDENTITY (auto-increment)
- **Configuração DDL:** validate (não altera tabelas automaticamente)

---

*Esta documentação serve como base para criação do modelo lógico e físico do banco de dados do sistema Slotify.*