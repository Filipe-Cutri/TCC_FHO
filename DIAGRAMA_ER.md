# Diagrama Entidade-Relacionamento (ER) - Sistema Slotfy

```
                           ┌─────────────────────────┐
                           │      ESTABLISHMENTS     │
                           │                         │
                           │ PK id (BIGINT)          │
                           │    name (VARCHAR)       │
                           │    email (VARCHAR)      │
                           │    phone (VARCHAR)      │
                           │    address (VARCHAR)    │
                           │    description (TEXT)   │
                           │    working_hours (VARCHAR)│
                           │    image_url (VARCHAR)  │
                           │    status (ENUM)        │
                           │    category (VARCHAR)   │
                           │    cnpj (VARCHAR)       │
                           │    settings (TEXT)      │
                           │    created_at (TIMESTAMP)│
                           │    updated_at (TIMESTAMP)│
                           └─────────────┬───────────┘
                                        │
                               ┌────────┼────────┐
                               │        │        │
                    ┌──────────▼──┐  ┌──▼──────┐ │
                    │ESTABLISHMENT│  │PROFESSION│ │
                    │    _USERS   │  │   ALS    │ │
                    │             │  │          │ │
                    │PK id        │  │PK id     │ │
                    │   name      │  │   name   │ │
                    │   email     │  │   email  │ │
                    │   password  │  │   phone  │ │
                    │   role(ENUM)│  │   special│ │
                    │FK establish.│  │   ties   │ │
                    │   _id       │  │FK establi│ │
                    │   active    │  │   sh_id  │ │
                    │   created_at│  │   rating │ │
                    │   updated_at│  │   total_ │ │
                    └─────────────┘  │   appoint│ │
                                     │   ments  │ │
                                     │   satisf_│ │
                                     │   rate   │ │
                                     │   status │ │
                                     │   image_ │ │
                                     │   url    │ │
                                     │   created│ │
                                     │   _at    │ │
                                     │   updated│ │
                                     │   _at    │ │
                                     └──────┬───┘ │
                                            │     │
                              ┌─────────────▼─────▼──────────┐
                              │           SERVICES           │
                              │                              │
                              │ PK id (BIGINT)               │
                              │    name (VARCHAR)            │
                              │    description (TEXT)        │
                              │    duration_minutes (INT)    │
                              │    price (DECIMAL)           │
                              │ FK establishment_id (BIGINT) │
                              │    created_at (TIMESTAMP)    │
                              │    updated_at (TIMESTAMP)    │
                              └──────────────┬───────────────┘
                                             │
                         ┌───────────────────┼───────────────────┐
                         │                   │                   │
              ┌──────────▼──────────┐        │        ┌─────────▼─────────┐
              │       CLIENTS       │        │        │    APPOINTMENTS   │
              │                     │        │        │                   │
              │ PK id (BIGINT)      │        │        │ PK id (BIGINT)    │
              │    name (VARCHAR)   │        │        │ FK client_id      │
              │    email (VARCHAR)  │        │        │ FK professional_id│
              │    password (VARCHAR)│       │        │ FK service_id     │
              │    phone (VARCHAR)  │        │        │ FK establishment_id│
              │    active (BOOLEAN) │        │        │    appointment_   │
              │    created_at       │        │        │    datetime       │
              │    updated_at       │        │        │    status (ENUM)  │
              └──────────┬──────────┘        │        │    notes (TEXT)   │
                         │                   │        │    created_at     │
                         │                   │        │    updated_at     │
                         │                   │        └─────────┬─────────┘
                         │                   │                  │
                         └───────────────────┼──────────────────┘
                                             │
                                             ▼
                                    [TABELA CENTRAL]
                                      APPOINTMENTS
                                   conecta todas as
                                   entidades do sistema

```

## Relacionamentos Detalhados

### 1. ESTABLISHMENTS (1) ←→ (N) ESTABLISHMENT_USERS
- **Tipo:** One-to-Many
- **FK:** establishment_users.establishment_id → establishments.id
- **Descrição:** Um estabelecimento pode ter vários usuários (admins/funcionários)

### 2. ESTABLISHMENTS (1) ←→ (N) PROFESSIONALS
- **Tipo:** One-to-Many  
- **FK:** professionals.establishment_id → establishments.id
- **Descrição:** Um estabelecimento pode ter vários profissionais

### 3. ESTABLISHMENTS (1) ←→ (N) SERVICES
- **Tipo:** One-to-Many
- **FK:** services.establishment_id → establishments.id
- **Descrição:** Um estabelecimento pode oferecer vários serviços

### 4. ESTABLISHMENTS (1) ←→ (N) APPOINTMENTS
- **Tipo:** One-to-Many
- **FK:** appointments.establishment_id → establishments.id
- **Descrição:** Um estabelecimento pode ter vários agendamentos

### 5. CLIENTS (1) ←→ (N) APPOINTMENTS
- **Tipo:** One-to-Many
- **FK:** appointments.client_id → clients.id
- **Descrição:** Um cliente pode ter vários agendamentos

### 6. PROFESSIONALS (1) ←→ (N) APPOINTMENTS
- **Tipo:** One-to-Many
- **FK:** appointments.professional_id → professionals.id
- **Descrição:** Um profissional pode ter vários agendamentos

### 7. SERVICES (1) ←→ (N) APPOINTMENTS
- **Tipo:** One-to-Many
- **FK:** appointments.service_id → services.id
- **Descrição:** Um serviço pode ser agendado várias vezes

## Cardinalidades e Regras de Negócio

```
ESTABLISHMENTS (1,N) ────────── ESTABLISHMENT_USERS (0,N)
ESTABLISHMENTS (1,N) ────────── PROFESSIONALS (0,N)
ESTABLISHMENTS (1,N) ────────── SERVICES (0,N)
ESTABLISHMENTS (1,N) ────────── APPOINTMENTS (0,N)

CLIENTS (1,N) ────────────────── APPOINTMENTS (0,N)
PROFESSIONALS (1,N) ─────────── APPOINTMENTS (0,N)
SERVICES (1,N) ──────────────── APPOINTMENTS (0,N)
```

### Regras Importantes:
1. **APPOINTMENTS é a tabela central** que conecta todas as outras entidades
2. **Cada appointment deve ter:** cliente, profissional, serviço e estabelecimento
3. **Integridade referencial:** Exclusão em cascata para manter consistência
4. **Auditoria:** Todas as tabelas têm campos created_at e updated_at
5. **Status:** Entidades principais têm controle de status (ativo/inativo)

## Enums do Sistema

### UserRole
- ADMIN ("Administrador")
- STAFF ("Funcionário")

### EstablishmentStatus
- ACTIVE ("Ativo")
- INACTIVE ("Inativo") 
- SUSPENDED ("Suspenso")
- PENDING ("Pendente")

### ProfessionalStatus
- ACTIVE ("Ativo")
- INACTIVE ("Inativo")
- SUSPENDED ("Suspenso")

### ServiceStatus
- ACTIVE ("Ativo")
- INACTIVE ("Inativo")
- SUSPENDED ("Suspenso")

### AppointmentStatus
- SCHEDULED ("Agendado")
- CONFIRMED ("Confirmado")
- IN_PROGRESS ("Em Andamento")
- COMPLETED ("Concluído")
- CANCELLED ("Cancelado")
- NO_SHOW ("Não Compareceu")