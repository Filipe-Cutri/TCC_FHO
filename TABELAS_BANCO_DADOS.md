# Tabelas do Sistema Slotify - Modelo Lógico e Físico

Este documento apresenta todas as tabelas presentes no sistema Slotify para auxiliar na criação do modelo lógico e físico do banco de dados.

## Visão Geral

O sistema utiliza **PostgreSQL** como banco de dados e **Spring Boot** com JPA/Hibernate para mapeamento objeto-relacional. Todas as entidades herdam de `BaseEntity` que fornece campos comuns de auditoria.

---

## 1. Tabela: `clients`

**Entidade:** `Client`  
**Descrição:** Armazena informações dos clientes que fazem agendamentos

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador único |
| `name` | VARCHAR(255) | NOT NULL | Nome do cliente |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Email do cliente |
| `password` | VARCHAR(255) | NOT NULL, MIN 6 chars | Senha (hash) |
| `phone` | VARCHAR(20) | NULLABLE | Telefone |
| `active` | BOOLEAN | DEFAULT true | Status ativo/inativo |
| `created_at` | TIMESTAMP | NOT NULL | Data de criação |
| `updated_at` | TIMESTAMP | NULLABLE | Data de atualização |

### Validações:
- Email deve ter formato válido
- Senha deve ter pelo menos 6 caracteres
- Nome é obrigatório

---

## 2. Tabela: `establishment_users`

**Entidade:** `EstablishmentUser`  
**Descrição:** Usuários administrativos dos estabelecimentos (admins e funcionários)

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador único |
| `name` | VARCHAR(255) | NOT NULL | Nome do usuário |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Email do usuário |
| `password` | VARCHAR(255) | NOT NULL, MIN 6 chars | Senha (hash) |
| `role` | VARCHAR(20) | NOT NULL | Papel: ADMIN ou STAFF |
| `establishment_id` | BIGINT | FOREIGN KEY | ID do estabelecimento |
| `active` | BOOLEAN | DEFAULT true | Status ativo/inativo |
| `created_at` | TIMESTAMP | NOT NULL | Data de criação |
| `updated_at` | TIMESTAMP | NULLABLE | Data de atualização |

### Relacionamentos:
- `establishment_id` → `establishments(id)`

### Enum Values - UserRole:
- `ADMIN` ("admin", "Administrador")
- `STAFF` ("staff", "Funcionário")

---

## 3. Tabela: `establishments`

**Entidade:** `Establishment`  
**Descrição:** Estabelecimentos que oferecem serviços

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador único |
| `name` | VARCHAR(255) | NOT NULL | Nome do estabelecimento |
| `email` | VARCHAR(255) | NULLABLE | Email do estabelecimento |
| `phone` | VARCHAR(20) | NULLABLE | Telefone |
| `address` | VARCHAR(500) | NULLABLE | Endereço completo |
| `description` | VARCHAR(1000) | NULLABLE | Descrição do estabelecimento |
| `working_hours` | VARCHAR(500) | NULLABLE | Horário de funcionamento |
| `image_url` | VARCHAR(500) | NULLABLE | URL da imagem |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Status do estabelecimento |
| `category` | VARCHAR(100) | NULLABLE | Categoria do negócio |
| `cnpj` | VARCHAR(14) | NULLABLE | CNPJ do estabelecimento |
| `settings` | TEXT | NULLABLE | Configurações em JSON |
| `created_at` | TIMESTAMP | NOT NULL | Data de criação |
| `updated_at` | TIMESTAMP | NULLABLE | Data de atualização |

### Validações:
- Email deve ter formato válido (se fornecido)
- Nome é obrigatório

### Enum Values - EstablishmentStatus:
- `ACTIVE` ("active", "Ativo")
- `INACTIVE` ("inactive", "Inativo")
- `SUSPENDED` ("suspended", "Suspenso")
- `PENDING` ("pending", "Pendente")

---

## 4. Tabela: `professionals`

**Entidade:** `Professional`  
**Descrição:** Profissionais que trabalham nos estabelecimentos

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador único |
| `name` | VARCHAR(255) | NOT NULL | Nome do profissional |
| `email` | VARCHAR(255) | NULLABLE | Email do profissional |
| `phone` | VARCHAR(20) | NULLABLE | Telefone |
| `specialties` | VARCHAR(500) | NULLABLE | Especialidades |
| `establishment_id` | BIGINT | NOT NULL, FOREIGN KEY | ID do estabelecimento |
| `rating` | DECIMAL(3,2) | MIN 0, MAX 5, DEFAULT 0 | Avaliação média |
| `total_appointments` | INTEGER | DEFAULT 0 | Total de agendamentos |
| `satisfaction_rate` | DECIMAL(5,2) | MIN 0, MAX 100, DEFAULT 0 | Taxa de satisfação |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Status do profissional |
| `image_url` | VARCHAR(500) | NULLABLE | URL da imagem |
| `created_at` | TIMESTAMP | NOT NULL | Data de criação |
| `updated_at` | TIMESTAMP | NULLABLE | Data de atualização |

### Relacionamentos:
- `establishment_id` → `establishments(id)`

### Validações:
- Email deve ter formato válido (se fornecido)
- Nome é obrigatório
- Rating entre 0 e 5
- Taxa de satisfação entre 0 e 100

### Enum Values - ProfessionalStatus:
- `ACTIVE` ("active", "Ativo")
- `INACTIVE` ("inactive", "Inativo")
- `SUSPENDED` ("suspended", "Suspenso")

---

## 5. Tabela: `services`

**Entidade:** `Service`  
**Descrição:** Serviços oferecidos pelos estabelecimentos

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador único |
| `name` | VARCHAR(255) | NOT NULL | Nome do serviço |
| `description` | VARCHAR(1000) | NULLABLE | Descrição do serviço |
| `duration_minutes` | INTEGER | NOT NULL, MIN 1 | Duração em minutos |
| `price` | DECIMAL(10,2) | NOT NULL, MIN 0 | Preço do serviço |
| `establishment_id` | BIGINT | NOT NULL, FOREIGN KEY | ID do estabelecimento |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Status do serviço |
| `category` | VARCHAR(100) | NULLABLE | Categoria do serviço |
| `image_url` | VARCHAR(500) | NULLABLE | URL da imagem |
| `created_at` | TIMESTAMP | NOT NULL | Data de criação |
| `updated_at` | TIMESTAMP | NULLABLE | Data de atualização |

### Relacionamentos:
- `establishment_id` → `establishments(id)`

### Validações:
- Nome é obrigatório
- Duração deve ser no mínimo 1 minuto
- Preço deve ser no mínimo 0

### Enum Values - ServiceStatus:
- `ACTIVE` ("active", "Ativo")
- `INACTIVE` ("inactive", "Inativo")
- `SUSPENDED` ("suspended", "Suspenso")

---

## 6. Tabela: `appointments`

**Entidade:** `Appointment`  
**Descrição:** Agendamentos de serviços

| Campo | Tipo | Constraints | Descrição |
|-------|------|-------------|-----------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador único |
| `client_id` | BIGINT | NOT NULL, FOREIGN KEY | ID do cliente |
| `professional_id` | BIGINT | NOT NULL, FOREIGN KEY | ID do profissional |
| `service_id` | BIGINT | NOT NULL, FOREIGN KEY | ID do serviço |
| `establishment_id` | BIGINT | NOT NULL, FOREIGN KEY | ID do estabelecimento |
| `appointment_datetime` | TIMESTAMP | NOT NULL | Data e hora do agendamento |
| `status` | VARCHAR(20) | DEFAULT 'SCHEDULED' | Status do agendamento |
| `notes` | VARCHAR(1000) | NULLABLE | Observações |
| `created_at` | TIMESTAMP | NOT NULL | Data de criação |
| `updated_at` | TIMESTAMP | NULLABLE | Data de atualização |

### Relacionamentos:
- `client_id` → `clients(id)`
- `professional_id` → `professionals(id)`
- `service_id` → `services(id)`
- `establishment_id` → `establishments(id)`

### Enum Values - AppointmentStatus:
- `SCHEDULED` ("scheduled", "Agendado")
- `CONFIRMED` ("confirmed", "Confirmado")
- `IN_PROGRESS` ("in_progress", "Em Andamento")
- `COMPLETED` ("completed", "Concluído")
- `CANCELLED` ("cancelled", "Cancelado")
- `NO_SHOW` ("no_show", "Não Compareceu")

---

## Diagrama de Relacionamentos

```
clients
├── appointments (client_id)

establishments
├── establishment_users (establishment_id)
├── professionals (establishment_id)
├── services (establishment_id)
└── appointments (establishment_id)

professionals
└── appointments (professional_id)

services
└── appointments (service_id)

appointments (tabela central com FK para todas as outras)
```

---

## Índices Recomendados

### Índices Únicos:
- `clients(email)`
- `establishment_users(email)`

### Índices de Performance:
- `appointments(client_id)`
- `appointments(professional_id)`
- `appointments(service_id)`
- `appointments(establishment_id)`
- `appointments(appointment_datetime)`
- `appointments(status)`
- `professionals(establishment_id)`
- `services(establishment_id)`
- `establishment_users(establishment_id)`

---

## Considerações Técnicas

### Auditoria:
Todas as tabelas herdam de `BaseEntity` e possuem:
- `created_at`: Data de criação (preenchido automaticamente)
- `updated_at`: Data de última atualização (atualizado automaticamente)

### Configuração JPA:
- Estratégia de geração de ID: `IDENTITY` (auto-increment)
- Modo DDL: `validate` (não cria/altera tabelas automaticamente)
- Dialeto: PostgreSQL

### Enums:
Todos os enums são armazenados como `VARCHAR` com os valores string dos enums.

---

## Scripts SQL de Criação (Exemplo)

```sql
-- Exemplo de script de criação das tabelas principais
CREATE TABLE establishments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    description VARCHAR(1000),
    working_hours VARCHAR(500),
    image_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    category VARCHAR(100),
    cnpj VARCHAR(14),
    settings TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Continue com as demais tabelas...
```

---

*Documento gerado com base na análise do código-fonte do sistema Slotify*  
*Data: Janeiro 2024*