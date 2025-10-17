# Funcionalidades Completas e Integrações Sistêmicas - Slotfy

## Visão Geral

Este documento detalha as funcionalidades completas, integrações Backend (API) e fluxos de interação com o Cliente Final para o sistema Slotfy - plataforma de gerenciamento para estabelecimentos de beleza (barbearias e salões).

## Índice

1. [Módulo de Clientes](#módulo-de-clientes)
2. [Módulo de Estabelecimentos](#módulo-de-estabelecimentos)
3. [Módulo de Profissionais](#módulo-de-profissionais)
4. [Módulo de Serviços](#módulo-de-serviços)
5. [Módulo de Agendamentos](#módulo-de-agendamentos)
6. [Módulo de Notificações](#módulo-de-notificações)
7. [Módulo de Pagamentos](#módulo-de-pagamentos)
8. [Módulo de Relatórios](#módulo-de-relatórios)
9. [Integrações com Cliente Final](#integrações-com-cliente-final)

---

## Módulo de Clientes

### 1.1 Tela: Cliente - Registro (`client-register.html`)

#### Funcionalidades (CRUD)
- **Criar (Create)**: Registro de novo cliente
- **Ler (Read)**: Verificação de email existente
- **Atualizar (Update)**: Não aplicável nesta tela
- **Deletar (Delete)**: Não aplicável nesta tela

#### Campos do Formulário
- Nome completo (obrigatório)
- Email (obrigatório, único)
- Telefone (opcional, com máscara)
- Senha (obrigatório, mínimo 6 caracteres)
- Confirmação de senha (obrigatório)
- Estabelecimento preferido (opcional, via seletor)

#### Integrações Backend

**Endpoint 1: Registrar Cliente**
```
POST /api/client/register
Content-Type: application/json

Request Body:
{
  "name": "João Silva",
  "email": "joao@example.com",
  "phone": "(11) 99999-9999",
  "password": "senha123",
  "selectedEstablishmentId": 1
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Cliente cadastrado com sucesso",
  "data": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@example.com",
    "phone": "(11) 99999-9999",
    "active": true,
    "selectedEstablishmentId": 1,
    "token": "eyJhbGc..."
  }
}

Response (Error - 400 Bad Request):
{
  "success": false,
  "message": "Email já cadastrado"
}
```

**Endpoint 2: Listar Estabelecimentos**
```
GET /api/client/establishments

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Barbearia do João",
      "imageUrl": "https://...",
      "address": "Rua X, 123",
      "category": "Barbearia",
      "status": "ACTIVE"
    }
  ],
  "count": 1
}
```

#### Validações Frontend
- Email deve ter formato válido
- Senhas devem coincidir
- Telefone deve ter formato válido (opcional)
- Todos os campos obrigatórios preenchidos

#### Fluxo de Interação
1. Cliente acessa página de registro
2. Preenche formulário
3. (Opcional) Clica em "Selecionar Estabelecimento" → Modal com lista
4. Seleciona estabelecimento preferido
5. Submete formulário
6. Sistema valida dados
7. Backend cria registro no banco
8. Backend envia email de boas-vindas (futuro)
9. Sessão criada automaticamente
10. Redireciona para tela de preferências

---

### 1.2 Tela: Cliente - Login (`client-login.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Autenticação de credenciais

#### Campos do Formulário
- Email (obrigatório)
- Senha (obrigatório)
- Lembrar-me (checkbox opcional)

#### Integrações Backend

**Endpoint: Login de Cliente**
```
POST /api/client/login
Content-Type: application/json

Request Body:
{
  "email": "joao@example.com",
  "password": "senha123"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Login realizado com sucesso",
  "data": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@example.com",
    "phone": "(11) 99999-9999",
    "selectedEstablishmentId": 1,
    "token": "eyJhbGc..."
  }
}

Response (Error - 401 Unauthorized):
{
  "success": false,
  "message": "Email ou senha incorretos"
}
```

#### Validações Frontend
- Email deve ter formato válido
- Senha não pode estar vazia

#### Fluxo de Interação
1. Cliente acessa página de login
2. Insere email e senha
3. Submete formulário
4. Sistema valida credenciais no backend
5. Backend retorna dados do usuário e token
6. Sessão criada no localStorage (24h)
7. Redireciona para dashboard do cliente

---

### 1.3 Tela: Cliente - Dashboard (`client-dashboard.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Visualizar dados do dashboard

#### Componentes da Tela
- Próximo agendamento
- Estatísticas pessoais
- Estabelecimentos favoritos
- Ações rápidas (novo agendamento, ver histórico)

#### Integrações Backend

**Endpoint: Dados do Dashboard**
```
GET /api/client/dashboard?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "client": {
      "id": 1,
      "name": "João Silva",
      "email": "joao@example.com",
      "phone": "(11) 99999-9999"
    },
    "stats": {
      "totalAppointments": 15,
      "favoriteProfessionals": 2
    },
    "nextAppointment": {
      "id": 123,
      "appointmentDateTime": "2025-10-20T14:00:00",
      "professionalName": "Carlos Barbeiro",
      "serviceName": "Corte + Barba",
      "establishmentName": "Barbearia do João",
      "status": "CONFIRMED"
    }
  }
}
```

**Endpoint: Próximo Agendamento**
```
GET /api/client/appointments/next?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "id": 123,
    "appointmentDateTime": "2025-10-20T14:00:00",
    "professionalName": "Carlos Barbeiro",
    "serviceName": "Corte + Barba",
    "establishmentName": "Barbearia do João",
    "status": "CONFIRMED"
  }
}
```

#### Fluxo de Interação
1. Cliente acessa dashboard
2. Sistema carrega dados via API
3. Exibe próximo agendamento (se existir)
4. Exibe estatísticas pessoais
5. Cliente pode clicar em ações rápidas
6. Sistema redireciona para tela apropriada

---

### 1.4 Tela: Cliente - Perfil (`client-profile.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Visualizar dados do perfil
- **Atualizar (Update)**: Editar informações pessoais
- **Deletar (Delete)**: Desativar conta (futuro)

#### Campos Editáveis
- Nome completo
- Telefone
- Foto de perfil (futuro)
- Preferências de notificação

#### Integrações Backend

**Endpoint 1: Obter Perfil**
```
GET /api/client/profile?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@example.com",
    "phone": "(11) 99999-9999"
  }
}
```

**Endpoint 2: Atualizar Perfil**
```
PUT /api/client/profile
Content-Type: application/json

Request Body:
{
  "clientId": 1,
  "name": "João Silva Santos",
  "phone": "(11) 98888-8888"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Perfil atualizado com sucesso",
  "data": {
    "id": 1,
    "name": "João Silva Santos",
    "email": "joao@example.com",
    "phone": "(11) 98888-8888"
  }
}
```

#### Validações Frontend
- Nome não pode estar vazio
- Telefone deve ter formato válido (se preenchido)

#### Fluxo de Interação
1. Cliente acessa perfil
2. Sistema carrega dados atuais
3. Cliente edita campos desejados
4. Clica em "Salvar"
5. Sistema valida dados
6. Backend atualiza registro
7. Mensagem de sucesso exibida
8. Sessão atualizada com novos dados

---

### 1.5 Tela: Cliente - Estabelecimentos (`client-establishments.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Listar estabelecimentos ativos
- **Ler (Read)**: Ver detalhes de estabelecimento

#### Componentes da Tela
- Grid/lista de estabelecimentos
- Filtros (categoria, localização)
- Busca por nome
- Cards com informações básicas

#### Integrações Backend

**Endpoint 1: Listar Estabelecimentos**
```
GET /api/client/establishments

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Barbearia do João",
      "imageUrl": "https://...",
      "address": "Rua X, 123",
      "category": "Barbearia",
      "status": "ACTIVE",
      "workingHours": "Seg-Sex: 9h-19h"
    }
  ],
  "count": 1
}
```

**Endpoint 2: Detalhes do Estabelecimento**
```
GET /api/client/establishments/1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Barbearia do João",
    "email": "contato@barbearia.com",
    "phone": "(11) 3333-3333",
    "address": "Rua X, 123, Centro - São Paulo/SP",
    "description": "A melhor barbearia da região",
    "workingHours": "Seg-Sex: 9h-19h, Sáb: 9h-15h",
    "imageUrl": "https://...",
    "category": "Barbearia"
  }
}
```

#### Fluxo de Interação
1. Cliente acessa lista de estabelecimentos
2. Sistema carrega estabelecimentos ativos
3. Cliente pode filtrar/buscar
4. Cliente clica em estabelecimento
5. Sistema carrega detalhes
6. Cliente pode ver serviços e profissionais
7. Cliente pode agendar serviço

---

### 1.6 Tela: Cliente - Serviços (`client-services.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Listar serviços de estabelecimento
- **Ler (Read)**: Ver detalhes de serviço

#### Integrações Backend

**Endpoint: Serviços de Estabelecimento**
```
GET /api/client/establishments/1/services

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Corte Masculino",
      "description": "Corte tradicional",
      "durationMinutes": 30,
      "price": 35.00,
      "category": "Corte",
      "imageUrl": "https://..."
    }
  ],
  "count": 1
}
```

#### Fluxo de Interação
1. Cliente seleciona estabelecimento
2. Sistema carrega serviços disponíveis
3. Cliente visualiza serviços com preços e duração
4. Cliente pode selecionar serviço para agendar

---

### 1.7 Tela: Cliente - Profissionais (`client-professionals.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Listar profissionais de estabelecimento

#### Integrações Backend

**Endpoint: Profissionais de Estabelecimento**
```
GET /api/client/establishments/1/professionals

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Carlos Barbeiro",
      "specialties": "Corte, Barba, Pigmentação",
      "rating": 4.8,
      "totalAppointments": 150,
      "satisfactionRate": 95.5,
      "imageUrl": "https://..."
    }
  ],
  "count": 1
}
```

#### Fluxo de Interação
1. Cliente seleciona estabelecimento
2. Sistema carrega profissionais ativos
3. Cliente visualiza avaliações e especialidades
4. Cliente pode selecionar profissional para agendar

---

### 1.8 Tela: Cliente - Agendamentos (`client-bookings.html`)

#### Funcionalidades (CRUD)
- **Criar (Create)**: Novo agendamento
- **Ler (Read)**: Listar agendamentos
- **Ler (Read)**: Ver detalhes de agendamento
- **Atualizar (Update)**: Cancelar agendamento
- **Deletar (Delete)**: Não aplicável (cancelamento é update de status)

#### Integrações Backend

**Endpoint 1: Criar Agendamento**
```
POST /api/client/appointments/book
Content-Type: application/json

Request Body:
{
  "clientId": 1,
  "professionalId": 2,
  "serviceId": 3,
  "establishmentId": 1,
  "appointmentDateTime": "2025-10-20T14:00:00",
  "notes": "Observações opcionais"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Agendamento criado com sucesso",
  "data": {
    "id": 123,
    "clientId": 1,
    "professionalId": 2,
    "serviceId": 3,
    "establishmentId": 1,
    "appointmentDateTime": "2025-10-20T14:00:00",
    "status": "SCHEDULED",
    "clientName": "João Silva",
    "professionalName": "Carlos Barbeiro",
    "serviceName": "Corte Masculino",
    "serviceDurationMinutes": 30,
    "servicePrice": 35.00
  }
}
```

**Endpoint 2: Listar Agendamentos**
```
GET /api/client/appointments?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 123,
      "appointmentDateTime": "2025-10-20T14:00:00",
      "status": "SCHEDULED",
      "professionalName": "Carlos Barbeiro",
      "serviceName": "Corte Masculino",
      "establishmentName": "Barbearia do João"
    }
  ],
  "count": 1
}
```

**Endpoint 3: Histórico de Agendamentos**
```
GET /api/client/appointments/history?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 120,
      "appointmentDateTime": "2025-10-15T14:00:00",
      "status": "COMPLETED",
      "professionalName": "Carlos Barbeiro",
      "serviceName": "Corte Masculino",
      "servicePrice": 35.00
    }
  ],
  "count": 1
}
```

**Endpoint 4: Detalhes do Agendamento**
```
GET /api/client/appointments/123?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "id": 123,
    "clientId": 1,
    "professionalId": 2,
    "serviceId": 3,
    "establishmentId": 1,
    "appointmentDateTime": "2025-10-20T14:00:00",
    "status": "SCHEDULED",
    "notes": "Observações",
    "clientName": "João Silva",
    "professionalName": "Carlos Barbeiro",
    "serviceName": "Corte Masculino",
    "serviceDurationMinutes": 30,
    "servicePrice": 35.00
  }
}
```

**Endpoint 5: Cancelar Agendamento**
```
PUT /api/client/appointments/123/cancel?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "message": "Agendamento cancelado com sucesso",
  "data": {
    "id": 123,
    "status": "CANCELLED"
  }
}
```

**Endpoint 6: Verificar Disponibilidade**
```
GET /api/client/establishments/1/availability?professionalId=2&dateTime=2025-10-20T14:00:00&durationMinutes=30

Response (Success - 200 OK):
{
  "success": true,
  "available": true,
  "message": "Horário disponível"
}
```

#### Validações Frontend
- Data/hora deve ser futura
- Profissional, serviço e estabelecimento obrigatórios
- Verificar disponibilidade antes de confirmar

#### Fluxo de Interação - Novo Agendamento
1. Cliente acessa tela de agendamentos
2. Clica em "Novo Agendamento"
3. Seleciona estabelecimento
4. Seleciona serviço
5. Seleciona profissional
6. Escolhe data e horário
7. Sistema verifica disponibilidade
8. Cliente confirma
9. Backend cria agendamento
10. Sistema envia confirmação por email/SMS
11. Redireciona para detalhes do agendamento

#### Fluxo de Interação - Cancelamento
1. Cliente acessa lista de agendamentos
2. Clica em agendamento ativo
3. Clica em "Cancelar"
4. Modal de confirmação
5. Cliente confirma cancelamento
6. Backend atualiza status
7. Sistema envia notificação ao estabelecimento
8. Mensagem de sucesso exibida

---

### 1.9 Tela: Cliente - Notificações (`client-notifications.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Listar notificações
- **Atualizar (Update)**: Marcar como lida

#### Tipos de Notificação
- Confirmação de agendamento
- Lembrete de agendamento (24h antes)
- Cancelamento de agendamento
- Promoções do estabelecimento
- Mensagens do estabelecimento

#### Integrações Backend

**Endpoint 1: Listar Notificações**
```
GET /api/client/notifications?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "type": "APPOINTMENT_REMINDER",
      "title": "Lembrete de Agendamento",
      "message": "Seu agendamento é amanhã às 14:00",
      "read": false,
      "createdAt": "2025-10-19T10:00:00",
      "appointmentId": 123
    }
  ],
  "count": 1,
  "unreadCount": 1
}
```

**Endpoint 2: Marcar como Lida**
```
PUT /api/client/notifications/1/read?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "message": "Notificação marcada como lida"
}
```

#### Fluxo de Interação
1. Cliente acessa notificações
2. Sistema carrega notificações não lidas
3. Cliente visualiza notificações
4. Notificações são marcadas como lidas
5. Cliente pode clicar para ver detalhes

---

### 1.10 Tela: Cliente - Pagamentos (`client-payments.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Histórico de pagamentos
- **Ler (Read)**: Métodos de pagamento salvos

#### Integrações Backend

**Endpoint: Histórico de Pagamentos**
```
GET /api/client/payments?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "appointmentId": 120,
      "amount": 35.00,
      "paymentMethod": "CREDIT_CARD",
      "status": "PAID",
      "paidAt": "2025-10-15T15:00:00",
      "serviceName": "Corte Masculino"
    }
  ],
  "count": 1,
  "totalPaid": 35.00
}
```

#### Fluxo de Interação
1. Cliente acessa histórico de pagamentos
2. Sistema carrega transações
3. Cliente visualiza detalhes de pagamentos
4. Cliente pode exportar comprovantes (futuro)

---

## Módulo de Estabelecimentos

### 2.1 Tela: Estabelecimento - Dashboard (`establishment-dashboard.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Visualizar métricas e KPIs

#### Componentes da Tela
- Estatísticas do dia (agendamentos, receita)
- Gráficos de desempenho
- Próximos agendamentos
- Alertas e notificações

#### Integrações Backend

**Endpoint: Dashboard Principal**
```
GET /api/establishment/dashboard?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "today": {
      "appointments": 12,
      "revenue": 420.00,
      "completedAppointments": 8,
      "cancelledAppointments": 1
    },
    "thisMonth": {
      "appointments": 250,
      "revenue": 8750.00,
      "newClients": 15
    },
    "nextAppointments": [
      {
        "id": 125,
        "time": "15:30",
        "clientName": "Maria Santos",
        "serviceName": "Corte Feminino",
        "professionalName": "Ana Silva"
      }
    ]
  }
}
```

#### Fluxo de Interação
1. Estabelecimento acessa dashboard
2. Sistema carrega métricas do dia
3. Exibe agendamentos próximos
4. Atualiza dados em tempo real (polling ou websockets)

---

### 2.2 Tela: Estabelecimento - Agendamentos (`establishment-appointments.html`)

#### Funcionalidades (CRUD)
- **Criar (Create)**: Novo agendamento manual
- **Ler (Read)**: Listar todos os agendamentos
- **Atualizar (Update)**: Modificar status, reagendar
- **Deletar (Delete)**: Cancelar agendamento

#### Integrações Backend

**Endpoint 1: Listar Agendamentos**
```
GET /api/establishment/appointments?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 123,
      "appointmentDateTime": "2025-10-20T14:00:00",
      "status": "SCHEDULED",
      "clientName": "João Silva",
      "professionalName": "Carlos Barbeiro",
      "serviceName": "Corte Masculino",
      "servicePrice": 35.00
    }
  ],
  "count": 1
}
```

**Endpoint 2: Agendamentos de Hoje**
```
GET /api/establishment/appointments/today?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [...],
  "count": 12
}
```

**Endpoint 3: Criar Agendamento**
```
POST /api/establishment/appointments
Content-Type: application/json

Request Body:
{
  "clientId": 1,
  "professionalId": 2,
  "serviceId": 3,
  "establishmentId": 1,
  "appointmentDateTime": "2025-10-20T14:00:00",
  "notes": "Cliente preferencial"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Agendamento criado com sucesso",
  "data": {...}
}
```

**Endpoint 4: Atualizar Status**
```
PUT /api/establishment/appointments/123/status?establishmentId=1
Content-Type: application/json

Request Body:
{
  "status": "confirmed"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Status atualizado com sucesso",
  "data": {...}
}
```

**Endpoint 5: Reagendar**
```
PUT /api/establishment/appointments/123/reschedule?establishmentId=1
Content-Type: application/json

Request Body:
{
  "newDateTime": "2025-10-21T14:00:00"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Agendamento reagendado com sucesso",
  "data": {...}
}
```

**Endpoint 6: Confirmar Agendamento**
```
PUT /api/establishment/appointments/123/confirm?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "message": "Agendamento confirmado com sucesso"
}
```

**Endpoint 7: Completar Agendamento**
```
PUT /api/establishment/appointments/123/complete?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "message": "Agendamento finalizado com sucesso"
}
```

**Endpoint 8: Cancelar Agendamento**
```
PUT /api/establishment/appointments/123/cancel?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "message": "Agendamento cancelado com sucesso"
}
```

#### Fluxo de Interação - Gestão de Agendamento
1. Estabelecimento acessa agenda
2. Visualiza agendamentos por dia/semana/mês
3. Pode filtrar por profissional, status
4. Clica em agendamento para detalhes
5. Pode atualizar status (confirmar, completar, cancelar)
6. Sistema notifica cliente sobre mudanças

---

### 2.3 Tela: Estabelecimento - Serviços (`establishment-services.html`)

#### Funcionalidades (CRUD)
- **Criar (Create)**: Cadastrar novo serviço
- **Ler (Read)**: Listar todos os serviços
- **Atualizar (Update)**: Editar serviço
- **Deletar (Delete)**: Remover/desativar serviço

#### Integrações Backend

**Endpoint 1: Listar Serviços**
```
GET /api/establishment/services?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Corte Masculino",
      "description": "Corte tradicional",
      "durationMinutes": 30,
      "price": 35.00,
      "category": "Corte",
      "status": "ACTIVE"
    }
  ],
  "count": 1
}
```

**Endpoint 2: Criar Serviço**
```
POST /api/establishment/services
Content-Type: application/json

Request Body:
{
  "name": "Corte + Barba",
  "description": "Corte completo com barba",
  "durationMinutes": 45,
  "price": 50.00,
  "establishmentId": 1,
  "category": "Combo"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Serviço criado com sucesso",
  "data": {...}
}
```

**Endpoint 3: Atualizar Serviço**
```
PUT /api/establishment/services/1?establishmentId=1
Content-Type: application/json

Request Body:
{
  "name": "Corte Masculino Premium",
  "description": "Corte premium com finalização",
  "durationMinutes": 40,
  "price": 45.00,
  "category": "Corte"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Serviço atualizado com sucesso",
  "data": {...}
}
```

**Endpoint 4: Deletar Serviço**
```
DELETE /api/establishment/services/1?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "message": "Serviço removido com sucesso"
}
```

**Endpoint 5: Atualizar Status**
```
PUT /api/establishment/services/1/status?establishmentId=1
Content-Type: application/json

Request Body:
{
  "status": "inactive"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Status atualizado com sucesso"
}
```

#### Fluxo de Interação
1. Estabelecimento acessa gestão de serviços
2. Visualiza todos os serviços cadastrados
3. Pode criar novo serviço via modal
4. Pode editar serviço existente
5. Pode desativar/ativar serviço
6. Serviços inativos não aparecem para clientes

---

### 2.4 Tela: Estabelecimento - Profissionais (`establishment-professionals.html`)

#### Funcionalidades (CRUD)
- **Criar (Create)**: Cadastrar novo profissional
- **Ler (Read)**: Listar todos os profissionais
- **Atualizar (Update)**: Editar profissional
- **Deletar (Delete)**: Remover/desativar profissional

#### Integrações Backend

**Endpoint 1: Listar Profissionais**
```
GET /api/establishment/professionals?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Carlos Barbeiro",
      "email": "carlos@email.com",
      "phone": "(11) 99999-9999",
      "specialties": "Corte, Barba",
      "rating": 4.8,
      "status": "ACTIVE"
    }
  ],
  "count": 1
}
```

**Endpoint 2: Criar Profissional**
```
POST /api/establishment/professionals
Content-Type: application/json

Request Body:
{
  "name": "Ana Silva",
  "email": "ana@email.com",
  "phone": "(11) 98888-8888",
  "specialties": "Corte Feminino, Coloração",
  "establishmentId": 1
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Profissional cadastrado com sucesso",
  "data": {...}
}
```

**Endpoint 3: Atualizar Profissional**
```
PUT /api/establishment/professionals/1?establishmentId=1
Content-Type: application/json

Request Body:
{
  "name": "Carlos Barbeiro Jr",
  "email": "carlos@email.com",
  "phone": "(11) 99999-9999",
  "specialties": "Corte, Barba, Pigmentação"
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Profissional atualizado com sucesso",
  "data": {...}
}
```

**Endpoint 4: Deletar Profissional**
```
DELETE /api/establishment/professionals/1?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "message": "Profissional removido com sucesso"
}
```

#### Fluxo de Interação
1. Estabelecimento acessa gestão de profissionais
2. Visualiza todos os profissionais
3. Pode cadastrar novo profissional
4. Pode editar informações
5. Pode desativar profissional
6. Sistema atualiza disponibilidade na agenda

---

### 2.5 Tela: Estabelecimento - Relatórios (`establishment-reports.html`)

#### Funcionalidades (CRUD)
- **Ler (Read)**: Visualizar relatórios e estatísticas

#### Tipos de Relatório
- Financeiro (receita, despesas)
- Agendamentos (total, taxa de conclusão)
- Profissionais (performance individual)
- Clientes (novos, recorrentes)
- Serviços (mais vendidos)

#### Integrações Backend

**Endpoint 1: Estatísticas de Agendamentos**
```
GET /api/establishment/appointments/statistics?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "total": 250,
    "today": 12,
    "thisMonth": 250,
    "monthlyRevenue": 8750.00
  }
}
```

**Endpoint 2: Performance de Profissionais**
```
GET /api/establishment/appointments/performance?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "professionalId": 1,
      "professionalName": "Carlos Barbeiro",
      "totalAppointments": 150,
      "completedAppointments": 140,
      "revenue": 4900.00
    }
  ]
}
```

**Endpoint 3: Estatísticas de Serviços**
```
GET /api/establishment/services/statistics?establishmentId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "total": 8,
    "active": 7,
    "inactive": 1,
    "categories": 3
  }
}
```

#### Fluxo de Interação
1. Estabelecimento acessa relatórios
2. Seleciona período de análise
3. Sistema gera relatórios
4. Exibe gráficos e tabelas
5. Pode exportar relatórios (PDF, Excel - futuro)

---

## Módulo de Notificações

### 3.1 Sistema de Notificações

#### Tipos de Notificação
1. **Confirmação de Agendamento**
   - Enviado: Imediatamente após criar agendamento
   - Canais: Email, SMS (futuro), Push (futuro)
   - Destinatário: Cliente

2. **Lembrete de Agendamento**
   - Enviado: 24h antes do agendamento
   - Canais: Email, SMS (futuro), Push (futuro)
   - Destinatário: Cliente

3. **Cancelamento de Agendamento**
   - Enviado: Imediatamente após cancelamento
   - Canais: Email, SMS (futuro)
   - Destinatários: Cliente e Estabelecimento

4. **Reagendamento**
   - Enviado: Imediatamente após reagendar
   - Canais: Email, SMS (futuro)
   - Destinatários: Cliente e Estabelecimento

5. **Conclusão de Serviço**
   - Enviado: Após marcar como completado
   - Canais: Email, Push (futuro)
   - Destinatário: Cliente (com solicitação de avaliação)

#### Integrações Backend (Futuro)

**Endpoint: Enviar Notificação**
```
POST /api/notifications/send
Content-Type: application/json

Request Body:
{
  "type": "APPOINTMENT_REMINDER",
  "recipientId": 1,
  "recipientType": "CLIENT",
  "appointmentId": 123,
  "channels": ["EMAIL", "SMS"]
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Notificação enviada com sucesso",
  "data": {
    "notificationId": 456,
    "sentAt": "2025-10-19T10:00:00",
    "channels": ["EMAIL", "SMS"]
  }
}
```

#### Fluxo de Notificação Automática
1. Evento ocorre (novo agendamento, cancelamento, etc.)
2. Sistema identifica tipo de evento
3. Sistema determina destinatários
4. Sistema formata mensagem
5. Sistema envia via canais configurados
6. Sistema registra envio no log
7. Sistema monitora falhas e retentar

---

## Integrações com Cliente Final

### 4.1 Comunicação Direta

#### Recursos de Comunicação
1. **Chat In-App (Futuro)**
   - Cliente pode conversar com estabelecimento
   - Estabelecimento pode responder dúvidas
   - Histórico de conversas

2. **Email Automatizado**
   - Confirmações
   - Lembretes
   - Promoções

3. **SMS (Futuro)**
   - Lembretes urgentes
   - Confirmações

4. **Push Notifications (Futuro)**
   - Notificações em tempo real
   - Promoções

### 4.2 Gerenciamento de Perfil do Cliente

#### Histórico de Serviços

**Endpoint: Histórico Completo**
```
GET /api/client/appointments/history?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": [
    {
      "id": 120,
      "appointmentDateTime": "2025-10-15T14:00:00",
      "status": "COMPLETED",
      "professionalName": "Carlos Barbeiro",
      "serviceName": "Corte Masculino",
      "servicePrice": 35.00,
      "establishmentName": "Barbearia do João",
      "rating": 5,
      "review": "Excelente serviço"
    }
  ],
  "count": 15,
  "totalSpent": 525.00,
  "favoriteService": "Corte Masculino",
  "favoriteProfessional": "Carlos Barbeiro"
}
```

#### Preferências do Cliente

**Endpoint: Atualizar Preferências**
```
PUT /api/client/preferences?clientId=1
Content-Type: application/json

Request Body:
{
  "notificationEmail": true,
  "notificationSms": true,
  "notificationPush": true,
  "favoriteEstablishments": [1, 3],
  "favoriteProfessionals": [2],
  "preferredDays": ["monday", "wednesday", "friday"],
  "preferredTimes": ["morning", "afternoon"]
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Preferências atualizadas com sucesso"
}
```

### 4.3 Sistema de Avaliação (Futuro)

**Endpoint: Avaliar Serviço**
```
POST /api/client/appointments/120/review
Content-Type: application/json

Request Body:
{
  "clientId": 1,
  "rating": 5,
  "review": "Excelente serviço, muito satisfeito!",
  "professionalRating": 5,
  "establishmentRating": 5
}

Response (Success - 200 OK):
{
  "success": true,
  "message": "Avaliação registrada com sucesso"
}
```

### 4.4 Programa de Fidelidade (Futuro)

**Endpoint: Pontos do Cliente**
```
GET /api/client/loyalty/points?clientId=1

Response (Success - 200 OK):
{
  "success": true,
  "data": {
    "totalPoints": 150,
    "pointsToNextReward": 50,
    "availableRewards": [
      {
        "id": 1,
        "name": "10% de desconto",
        "pointsCost": 100
      }
    ]
  }
}
```

---

## Resumo de Endpoints por Módulo

### Cliente (Público)
- `POST /api/client/register` - Registro
- `POST /api/client/login` - Login
- `GET /api/client/dashboard` - Dashboard
- `GET /api/client/profile` - Perfil
- `PUT /api/client/profile` - Atualizar perfil
- `GET /api/client/establishments` - Listar estabelecimentos
- `GET /api/client/establishments/{id}` - Detalhes estabelecimento
- `GET /api/client/establishments/{id}/services` - Serviços
- `GET /api/client/establishments/{id}/professionals` - Profissionais
- `GET /api/client/establishments/{id}/availability` - Disponibilidade
- `POST /api/client/appointments/book` - Criar agendamento
- `GET /api/client/appointments` - Listar agendamentos
- `GET /api/client/appointments/history` - Histórico
- `GET /api/client/appointments/{id}` - Detalhes agendamento
- `PUT /api/client/appointments/{id}/cancel` - Cancelar
- `GET /api/client/notifications` - Notificações
- `PUT /api/client/notifications/{id}/read` - Marcar lida
- `GET /api/client/payments` - Histórico pagamentos

### Estabelecimento (Restrito)
- `POST /api/establishment/login` - Login
- `POST /api/establishment/register` - Registro
- `GET /api/establishment/dashboard` - Dashboard
- `GET /api/establishment/appointments` - Listar agendamentos
- `GET /api/establishment/appointments/today` - Agendamentos hoje
- `POST /api/establishment/appointments` - Criar agendamento
- `GET /api/establishment/appointments/{id}` - Detalhes
- `PUT /api/establishment/appointments/{id}/status` - Atualizar status
- `PUT /api/establishment/appointments/{id}/reschedule` - Reagendar
- `PUT /api/establishment/appointments/{id}/confirm` - Confirmar
- `PUT /api/establishment/appointments/{id}/complete` - Completar
- `PUT /api/establishment/appointments/{id}/cancel` - Cancelar
- `GET /api/establishment/appointments/statistics` - Estatísticas
- `GET /api/establishment/services` - Listar serviços
- `POST /api/establishment/services` - Criar serviço
- `PUT /api/establishment/services/{id}` - Atualizar serviço
- `DELETE /api/establishment/services/{id}` - Deletar serviço
- `PUT /api/establishment/services/{id}/status` - Atualizar status
- `GET /api/establishment/professionals` - Listar profissionais
- `POST /api/establishment/professionals` - Criar profissional
- `PUT /api/establishment/professionals/{id}` - Atualizar profissional
- `DELETE /api/establishment/professionals/{id}` - Deletar profissional

---

## Considerações de Implementação

### Segurança
- Todos os endpoints devem validar autenticação via token JWT
- Validar que o cliente/estabelecimento tem permissão para acessar o recurso
- Sanitizar todos os inputs para prevenir SQL Injection e XSS
- Rate limiting para prevenir abuso de APIs

### Performance
- Implementar cache para dados frequentemente acessados
- Paginação para listagens grandes
- Índices no banco de dados para queries frequentes
- Compressão de respostas HTTP

### Usabilidade
- Loading states durante requisições
- Feedback visual imediato para ações do usuário
- Mensagens de erro claras e acionáveis
- Design responsivo para mobile

### Monitoramento
- Logging de todas as operações importantes
- Métricas de uso da API
- Alertas para erros críticos
- Analytics de comportamento do usuário

---

## Próximos Passos

1. ✅ Documentação completa criada
2. ⏳ Implementar endpoints faltantes no backend
3. ⏳ Integrar frontend com novos endpoints
4. ⏳ Implementar sistema de notificações
5. ⏳ Adicionar testes automatizados
6. ⏳ Implementar sistema de avaliações
7. ⏳ Adicionar programa de fidelidade
8. ⏳ Implementar chat in-app
9. ⏳ Deploy em produção
10. ⏳ Monitoramento e melhorias contínuas

---

*Documento gerado em: Outubro 2025*  
*Versão: 1.0*  
*Autor: Sistema Slotfy - Equipe de Desenvolvimento*
