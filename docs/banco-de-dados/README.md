# Banco de Dados - PostgreSQL

## Visão Geral

O sistema utiliza **PostgreSQL** como banco de dados relacional. O schema é gerenciado através de migrações do Flyway.

## Schema do Banco

### Tabelas Principais

#### 1. establishments (Estabelecimentos)
Armazena informações sobre barbearias e salões de beleza.

**Campos principais:**
- `id`: Identificador único
- `name`: Nome do estabelecimento
- `email`, `phone`, `address`: Informações de contato
- `description`: Descrição do estabelecimento
- `working_hours`: Horário de funcionamento
- `image_url`: Logo/foto do estabelecimento
- `status`: ACTIVE, INACTIVE, SUSPENDED, PENDING
- `category`: Categoria do estabelecimento
- `cnpj`: CNPJ (14 caracteres)

#### 2. clients (Clientes)
Cadastro de clientes que utilizam o sistema para agendar serviços.

**Campos principais:**
- `id`: Identificador único
- `name`: Nome do cliente
- `email`: E-mail (único)
- `password`: Senha (hash, mínimo 6 caracteres)
- `phone`: Telefone
- `active`: Status ativo/inativo

#### 3. establishment_users (Usuários do Estabelecimento)
Usuários administrativos dos estabelecimentos.

**Campos principais:**
- `id`: Identificador único
- `name`: Nome do usuário
- `email`: E-mail (único)
- `password`: Senha (hash, mínimo 6 caracteres)
- `role`: ADMIN ou STAFF
- `establishment_id`: Referência ao estabelecimento

#### 4. professionals (Profissionais)
Profissionais que prestam serviços (barbeiros, cabeleireiros, etc.).

**Campos principais:**
- `id`: Identificador único
- `name`: Nome do profissional
- `email`, `phone`: Contato
- `specialties`: Especialidades
- `establishment_id`: Estabelecimento onde trabalha
- `rating`: Avaliação (0-5)
- `total_appointments`: Total de atendimentos
- `satisfaction_rate`: Taxa de satisfação (0-100%)
- `status`: ACTIVE, INACTIVE, SUSPENDED
- `image_url`: Foto do profissional

#### 5. services (Serviços)
Serviços oferecidos pelos estabelecimentos.

**Campos principais:**
- `id`: Identificador único
- `name`: Nome do serviço
- `description`: Descrição
- `duration_minutes`: Duração em minutos (mínimo 1)
- `price`: Preço (decimal, >= 0)
- `establishment_id`: Estabelecimento que oferece
- `status`: ACTIVE, INACTIVE, SUSPENDED
- `category`: Categoria do serviço
- `image_url`: Imagem do serviço

#### 6. appointments (Agendamentos)
Agendamentos de serviços.

**Campos principais:**
- `id`: Identificador único
- `client_id`: Cliente que agendou
- `professional_id`: Profissional selecionado
- `service_id`: Serviço agendado
- `establishment_id`: Estabelecimento
- `appointment_datetime`: Data e hora do agendamento
- `status`: SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
- `notes`: Observações

**Constraints:**
- `appointment_datetime` deve ser no futuro em relação a `created_at`

## Relacionamentos

```
establishments (1) ──┬──> (N) professionals
                     ├──> (N) services
                     ├──> (N) appointments
                     └──> (N) establishment_users

clients (1) ───────> (N) appointments

professionals (1) ──> (N) appointments

services (1) ───────> (N) appointments
```

## Índices

### Índices Únicos
- `idx_clients_email`: Email do cliente
- `idx_establishment_users_email`: Email do usuário do estabelecimento

### Índices de Performance
- `idx_appointments_client_id`: Busca por cliente
- `idx_appointments_professional_id`: Busca por profissional
- `idx_appointments_service_id`: Busca por serviço
- `idx_appointments_establishment_id`: Busca por estabelecimento
- `idx_appointments_datetime`: Busca por data/hora
- `idx_appointments_status`: Busca por status
- `idx_professionals_establishment_id`: Profissionais por estabelecimento
- `idx_services_establishment_id`: Serviços por estabelecimento

## Migrações (Flyway)

As migrações são gerenciadas pelo Flyway e ficam em:
```
back-end/src/main/resources/db/migration/
```

### Configuração
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

### Scripts Disponíveis

**No diretório `/database`:**
- `database_schema.sql`: Schema completo do banco
- `database_migration_add_establishment_to_client.sql`: Migração para adicionar relacionamento establishment em clients

## Validações e Constraints

### E-mail
Formato validado por regex: `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`

### Senhas
Mínimo de 6 caracteres (aplicado via constraint)

### Ratings
- `rating`: Decimal entre 0 e 5
- `satisfaction_rate`: Decimal entre 0 e 100

### Status
Valores permitidos através de CHECK constraints para garantir integridade

### Deleções em Cascata
- Ao deletar um estabelecimento:
  - Profissionais, serviços e agendamentos são deletados (CASCADE)
  - Usuários têm o `establishment_id` setado para NULL (SET NULL)

- Ao deletar um cliente:
  - Seus agendamentos são deletados (CASCADE)

## Conexão

### Desenvolvimento Local
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/slotfy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Produção (Railway)
As variáveis são injetadas automaticamente pelo Railway:
```properties
spring.datasource.url=${JDBC_DATABASE_URL}
```

## Backup e Restore

### Backup
```bash
pg_dump -h localhost -U postgres -d slotfy_db > backup.sql
```

### Restore
```bash
psql -h localhost -U postgres -d slotfy_db < backup.sql
```
