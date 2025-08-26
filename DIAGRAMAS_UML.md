# Diagramas UML - Sistema Slotfy

Este documento apresenta os diagramas UML do sistema Slotfy em formato textual para posterior conversão para UML visual.

## 1. Diagrama de Casos de Uso - Sistema de Agendamento

### Atores
- **Cliente**: Usuário que agenda serviços
- **Funcionário**: Funcionário do estabelecimento (staff)
- **Administrador**: Gerente/proprietário do estabelecimento
- **Sistema IA**: Sistema inteligente de sugestão de agendamentos

### Casos de Uso Principais

#### UC001 - Gerenciar Autenticação
**Atores**: Cliente, Funcionário, Administrador
**Descrição**: Permite login, logout e recuperação de senha
**Fluxo Principal**:
1. Usuário acessa sistema
2. Informa credenciais (email/senha)
3. Sistema valida autenticação
4. Sistema cria sessão de usuário

**Extensões**:
- UC001.1 - Recuperar Senha
- UC001.2 - Primeira Autenticação

#### UC002 - Visualizar Serviços Disponíveis
**Atores**: Cliente
**Descrição**: Cliente visualiza catálogo de serviços oferecidos
**Fluxo Principal**:
1. Cliente acessa área de serviços
2. Sistema exibe lista de serviços ativos
3. Sistema mostra detalhes (preço, duração, descrição)

#### UC003 - Criar Agendamento Manual
**Atores**: Cliente, Funcionário, Administrador
**Descrição**: Cria novo agendamento selecionando manualmente data/hora
**Fluxo Principal**:
1. Usuário seleciona serviço desejado
2. Usuário escolhe profissional
3. Usuário seleciona data e horário
4. Sistema valida disponibilidade
5. Sistema verifica conflitos
6. Sistema confirma agendamento

**Pré-condições**: Usuário autenticado
**Pós-condições**: Agendamento criado com status "Agendado"

**Extensões**:
- UC003.1 - Validar Conflito de Horário
- UC003.2 - Sugerir Horários Alternativos

#### UC004 - Agendamento Inteligente com IA
**Atores**: Cliente, Sistema IA
**Descrição**: IA sugere melhor horário baseado em preferências e disponibilidade
**Fluxo Principal**:
1. Cliente seleciona "Deixe a IA Escolher"
2. Sistema IA analisa preferências do cliente
3. Sistema IA verifica disponibilidade dos profissionais
4. Sistema IA calcula melhor horário
5. Sistema apresenta sugestão ao cliente
6. Cliente confirma ou rejeita sugestão

**Includes**: UC003 - Criar Agendamento Manual

#### UC005 - Visualizar Agendamentos
**Atores**: Cliente, Funcionário, Administrador
**Descrição**: Exibe lista de agendamentos conforme perfil do usuário
**Fluxo Principal**:
1. Usuário acessa área de agendamentos
2. Sistema filtra agendamentos por usuário/estabelecimento
3. Sistema exibe lista com detalhes

**Variações**:
- Cliente: vê apenas seus próprios agendamentos
- Funcionário/Admin: vê agendamentos do estabelecimento

#### UC006 - Gerenciar Status do Agendamento
**Atores**: Funcionário, Administrador
**Descrição**: Atualiza status do agendamento conforme fluxo
**Fluxo Principal**:
1. Funcionário/Admin acessa agendamento
2. Sistema exibe status atual
3. Funcionário/Admin seleciona novo status
4. Sistema valida transição de status
5. Sistema atualiza agendamento

**Fluxo de Status**: Agendado → Confirmado → Em Andamento → Concluído

**Extensões**:
- UC006.1 - Cancelar Agendamento
- UC006.2 - Marcar como Não Compareceu

#### UC007 - Reagendar Compromisso
**Atores**: Cliente, Funcionário, Administrador
**Descrição**: Altera data/hora de agendamento existente
**Fluxo Principal**:
1. Usuário seleciona agendamento para reagendar
2. Usuário escolhe nova data/hora
3. Sistema valida nova disponibilidade
4. Sistema verifica conflitos
5. Sistema atualiza agendamento

**Includes**: UC003.1 - Validar Conflito de Horário

#### UC008 - Cancelar Agendamento
**Atores**: Cliente, Funcionário, Administrador
**Descrição**: Cancela agendamento existente
**Fluxo Principal**:
1. Usuário seleciona agendamento
2. Usuário confirma cancelamento
3. Sistema altera status para "Cancelado"

#### UC009 - Gerenciar Profissionais
**Atores**: Administrador
**Descrição**: Cadastra e gerencia profissionais do estabelecimento
**Fluxo Principal**:
1. Admin acessa gestão de profissionais
2. Admin cadastra/edita informações do profissional
3. Sistema valida dados
4. Sistema salva informações

#### UC010 - Gerenciar Serviços
**Atores**: Administrador
**Descrição**: Cadastra e gerencia serviços oferecidos
**Fluxo Principal**:
1. Admin acessa gestão de serviços
2. Admin cadastra/edita serviço (nome, preço, duração)
3. Sistema valida dados
4. Sistema salva serviço

#### UC011 - Visualizar Dashboard e Relatórios
**Atores**: Funcionário, Administrador
**Descrição**: Exibe métricas e estatísticas do estabelecimento
**Fluxo Principal**:
1. Usuário acessa dashboard
2. Sistema calcula métricas (agendamentos, receita, performance)
3. Sistema exibe relatórios

### Relacionamentos entre Casos de Uso

**Includes**:
- UC004 includes UC003 (Agendamento IA inclui criação manual)
- UC007 includes UC003.1 (Reagendamento inclui validação de conflito)

**Extends**:
- UC001.1 extends UC001 (Recuperação de senha estende autenticação)
- UC003.1 extends UC003 (Validação de conflito estende criação)
- UC003.2 extends UC003 (Sugestão de horários estende criação)
- UC006.1 extends UC006 (Cancelamento estende gestão de status)
- UC006.2 extends UC006 (Não compareceu estende gestão de status)

**Generalization**:
- Funcionário e Administrador são especializações de "Usuário do Estabelecimento"

---

## 2. Diagrama de Classes - Sistema Slotfy

### Classes Principais

#### BaseEntity (Abstract)
```
BaseEntity
-----------
- id: Long
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
-----------
+ getId(): Long
+ setId(Long): void
+ getCreatedAt(): LocalDateTime
+ setCreatedAt(LocalDateTime): void
+ getUpdatedAt(): LocalDateTime  
+ setUpdatedAt(LocalDateTime): void
```

#### Client
```
Client extends BaseEntity
-----------
- name: String
- email: String  
- password: String
- phone: String
- active: Boolean
-----------
+ Client()
+ Client(name: String, email: String, password: String)
+ getName(): String
+ setName(String): void
+ getEmail(): String
+ setEmail(String): void
+ getPassword(): String
+ setPassword(String): void
+ getPhone(): String
+ setPhone(String): void
+ getActive(): Boolean
+ setActive(Boolean): void
```

#### EstablishmentUser
```
EstablishmentUser extends BaseEntity
-----------
- name: String
- email: String
- password: String
- role: UserRole
- establishmentId: Long
- active: Boolean
-----------
+ EstablishmentUser()
+ EstablishmentUser(name: String, email: String, password: String, role: UserRole, establishmentId: Long)
+ getName(): String
+ setName(String): void
+ getEmail(): String
+ setEmail(String): void
+ getPassword(): String
+ setPassword(String): void
+ getRole(): UserRole
+ setRole(UserRole): void
+ getEstablishmentId(): Long
+ setEstablishmentId(Long): void
+ getActive(): Boolean
+ setActive(Boolean): void
+ isAdmin(): boolean
+ isStaff(): boolean
```

#### Establishment
```
Establishment extends BaseEntity
-----------
- name: String
- email: String
- phone: String
- address: String
- description: String
- workingHours: String
- imageUrl: String
- status: EstablishmentStatus
- category: String
- cnpj: String
- settings: String
-----------
+ Establishment()
+ Establishment(name: String, email: String, phone: String, address: String)
+ getName(): String
+ setName(String): void
+ getEmail(): String
+ setEmail(String): void
+ getPhone(): String
+ setPhone(String): void
+ getAddress(): String
+ setAddress(String): void
+ getDescription(): String
+ setDescription(String): void
+ getWorkingHours(): String
+ setWorkingHours(String): void
+ getImageUrl(): String
+ setImageUrl(String): void
+ getStatus(): EstablishmentStatus
+ setStatus(EstablishmentStatus): void
+ getCategory(): String
+ setCategory(String): void
+ getCnpj(): String
+ setCnpj(String): void
+ getSettings(): String
+ setSettings(String): void
+ isActive(): boolean
```

#### Professional
```
Professional extends BaseEntity
-----------
- name: String
- email: String
- phone: String
- specialties: String
- establishmentId: Long
- rating: BigDecimal
- totalAppointments: Integer
- satisfactionRate: BigDecimal
- status: ProfessionalStatus
- imageUrl: String
-----------
+ Professional()
+ Professional(name: String, email: String, phone: String, specialties: String, establishmentId: Long)
+ getName(): String
+ setName(String): void
+ getEmail(): String
+ setEmail(String): void
+ getPhone(): String
+ setPhone(String): void
+ getSpecialties(): String
+ setSpecialties(String): void
+ getEstablishmentId(): Long
+ setEstablishmentId(Long): void
+ getRating(): BigDecimal
+ setRating(BigDecimal): void
+ getTotalAppointments(): Integer
+ setTotalAppointments(Integer): void
+ getSatisfactionRate(): BigDecimal
+ setSatisfactionRate(BigDecimal): void
+ getStatus(): ProfessionalStatus
+ setStatus(ProfessionalStatus): void
+ getImageUrl(): String
+ setImageUrl(String): void
+ isActive(): boolean
+ incrementAppointments(): void
```

#### Service
```
Service extends BaseEntity
-----------
- name: String
- description: String
- durationMinutes: Integer
- price: BigDecimal
- establishmentId: Long
- status: ServiceStatus
- category: String
- imageUrl: String
-----------
+ Service()
+ Service(name: String, description: String, durationMinutes: Integer, price: BigDecimal, establishmentId: Long)
+ getName(): String
+ setName(String): void
+ getDescription(): String
+ setDescription(String): void
+ getDurationMinutes(): Integer
+ setDurationMinutes(Integer): void
+ getPrice(): BigDecimal
+ setPrice(BigDecimal): void
+ getEstablishmentId(): Long
+ setEstablishmentId(Long): void
+ getStatus(): ServiceStatus
+ setStatus(ServiceStatus): void
+ getCategory(): String
+ setCategory(String): void
+ getImageUrl(): String
+ setImageUrl(String): void
+ isActive(): boolean
+ getFormattedPrice(): String
+ getFormattedDuration(): String
```

#### Appointment
```
Appointment extends BaseEntity
-----------
- clientId: Long
- professionalId: Long
- serviceId: Long
- establishmentId: Long
- appointmentDateTime: LocalDateTime
- status: AppointmentStatus
- notes: String
- clientName: String
- professionalName: String
- serviceName: String
- serviceDurationMinutes: Integer
- servicePrice: BigDecimal
-----------
+ Appointment()
+ Appointment(clientId: Long, professionalId: Long, serviceId: Long, establishmentId: Long, appointmentDateTime: LocalDateTime)
+ getClientId(): Long
+ setClientId(Long): void
+ getProfessionalId(): Long
+ setProfessionalId(Long): void
+ getServiceId(): Long
+ setServiceId(Long): void
+ getEstablishmentId(): Long
+ setEstablishmentId(Long): void
+ getAppointmentDateTime(): LocalDateTime
+ setAppointmentDateTime(LocalDateTime): void
+ getStatus(): AppointmentStatus
+ setStatus(AppointmentStatus): void
+ getNotes(): String
+ setNotes(String): void
+ getClientName(): String
+ setClientName(String): void
+ getProfessionalName(): String
+ setProfessionalName(String): void
+ getServiceName(): String
+ setServiceName(String): void
+ getServiceDurationMinutes(): Integer
+ setServiceDurationMinutes(Integer): void
+ getServicePrice(): BigDecimal
+ setServicePrice(BigDecimal): void
+ isScheduled(): boolean
+ isConfirmed(): boolean
+ isCompleted(): boolean
+ isCancelled(): boolean
```

### Enumerações

#### UserRole
```
UserRole (enum)
-----------
ADMIN("admin", "Administrador")
STAFF("staff", "Funcionário")
-----------
- code: String
- description: String
-----------
+ getCode(): String
+ getDescription(): String
+ fromCode(String): UserRole
```

#### AppointmentStatus
```
AppointmentStatus (enum)
-----------
SCHEDULED("scheduled", "Agendado")
CONFIRMED("confirmed", "Confirmado")
IN_PROGRESS("in_progress", "Em Andamento")
COMPLETED("completed", "Concluído")
CANCELLED("cancelled", "Cancelado")
NO_SHOW("no_show", "Não Compareceu")
-----------
- code: String
- description: String
-----------
+ getCode(): String
+ getDescription(): String
+ fromCode(String): AppointmentStatus
```

#### EstablishmentStatus
```
EstablishmentStatus (enum)
-----------
ACTIVE("active", "Ativo")
INACTIVE("inactive", "Inativo")
-----------
- code: String
- description: String
-----------
+ getCode(): String
+ getDescription(): String
```

#### ProfessionalStatus
```
ProfessionalStatus (enum)
-----------
ACTIVE("active", "Ativo")
INACTIVE("inactive", "Inativo")
-----------
- code: String
- description: String
-----------
+ getCode(): String
+ getDescription(): String
```

#### ServiceStatus
```
ServiceStatus (enum)
-----------
ACTIVE("active", "Ativo")
INACTIVE("inactive", "Inativo")
-----------
- code: String
- description: String
-----------
+ getCode(): String
+ getDescription(): String
```

### Classes de Serviço (Service Layer)

#### AppointmentService
```
AppointmentService extends BaseService<Appointment, Long>
-----------
- appointmentRepository: AppointmentRepository
-----------
+ createAppointment(clientId: Long, professionalId: Long, serviceId: Long, establishmentId: Long, 
                   appointmentDateTime: LocalDateTime, notes: String, clientName: String,
                   professionalName: String, serviceName: String, serviceDurationMinutes: Integer,
                   servicePrice: BigDecimal): Appointment
+ reschedule(appointmentId: Long, newDateTime: LocalDateTime): Appointment
+ updateStatus(appointmentId: Long, status: AppointmentStatus): Appointment
+ cancel(appointmentId: Long): Appointment
+ findConflictingAppointments(professionalId: Long, startTime: LocalDateTime, endTime: LocalDateTime): List<Appointment>
+ findByEstablishmentId(establishmentId: Long): List<Appointment>
+ findByClientId(clientId: Long): List<Appointment>
+ findByStatus(status: AppointmentStatus): List<Appointment>
+ findTodayAppointments(establishmentId: Long): List<Appointment>
```

#### ClientService
```
ClientService extends BaseService<Client, Long>
-----------
- clientRepository: ClientRepository
- passwordEncoder: PasswordEncoder
-----------
+ register(name: String, email: String, password: String, phone: String): Client
+ authenticate(email: String, password: String): Client
+ findByEmail(email: String): Optional<Client>
+ updatePassword(clientId: Long, newPassword: String): void
+ deactivate(clientId: Long): void
```

#### EstablishmentUserService
```
EstablishmentUserService extends BaseService<EstablishmentUser, Long>
-----------
- establishmentUserRepository: EstablishmentUserRepository
- passwordEncoder: PasswordEncoder
-----------
+ register(name: String, email: String, password: String, role: UserRole, establishmentId: Long): EstablishmentUser
+ authenticate(email: String, password: String): EstablishmentUser
+ findByEmail(email: String): Optional<EstablishmentUser>
+ createStaffUser(adminId: Long, name: String, email: String, password: String): EstablishmentUser
+ findByEstablishmentId(establishmentId: Long): List<EstablishmentUser>
```

### Relacionamentos

#### Associações
1. **Establishment 1 ---- * EstablishmentUser**
   - Um estabelecimento tem muitos usuários
   - EstablishmentUser.establishmentId → Establishment.id

2. **Establishment 1 ---- * Professional**
   - Um estabelecimento tem muitos profissionais
   - Professional.establishmentId → Establishment.id

3. **Establishment 1 ---- * Service**
   - Um estabelecimento oferece muitos serviços
   - Service.establishmentId → Establishment.id

4. **Client 1 ---- * Appointment**
   - Um cliente pode ter muitos agendamentos
   - Appointment.clientId → Client.id

5. **Professional 1 ---- * Appointment**
   - Um profissional pode ter muitos agendamentos
   - Appointment.professionalId → Professional.id

6. **Service 1 ---- * Appointment**
   - Um serviço pode ser usado em muitos agendamentos
   - Appointment.serviceId → Service.id

7. **Establishment 1 ---- * Appointment**
   - Um estabelecimento tem muitos agendamentos
   - Appointment.establishmentId → Establishment.id

#### Dependências
- **Service Classes** dependem das **Entity Classes**
- **Controller Classes** dependem das **Service Classes**
- **Repository Classes** são dependências das **Service Classes**

#### Herança
- Todas as entidades principais herdam de **BaseEntity**
- **EstablishmentUser** pode ser especializado em **Admin** e **Staff** (via enum UserRole)

---

## 3. Diagrama de Atividades - Processo de Agendamento

### Fluxo Principal: Agendamento Manual

```
[Início]
    ↓
[Cliente faz login]
    ↓
[Acessar página de serviços]
    ↓
[Visualizar serviços disponíveis]
    ↓
[Selecionar serviço desejado]
    ↓
[Escolher profissional]
    ↓
{Método de agendamento?}
    ↓                    ↓
[Manual]            [IA Automática]
    ↓                    ↓
[Selecionar data]    [IA analisa preferências]
    ↓                    ↓
[Selecionar horário] [IA verifica disponibilidade]
    ↓                    ↓
[Validar disponibilidade] ←──────────┘
    ↓
{Horário disponível?}
    ↓No              ↓Sim
[Exibir conflito]  [Criar agendamento]
    ↓                    ↓
[Sugerir alternativas] [Salvar no banco]
    ↓                    ↓
[Voltar para seleção] [Enviar confirmação]
    ↓                    ↓
    └──────→         [Status: Agendado]
                         ↓
                    [Fim]
```

### Subfluxo: Agendamento com IA

```
[Cliente clica "Deixe a IA Escolher"]
    ↓
[IA busca histórico do cliente]
    ↓
[IA analisa preferências salvas]
    ↓
{Cliente tem preferências?}
    ↓No                    ↓Sim
[Usar configurações padrão] [Usar preferências do cliente]
    ↓                           ↓
    └──────→ [IA verifica disponibilidade dos profissionais] ←──────┘
                    ↓
            [IA calcula melhor horário baseado em:]
            - Intervalo preferido
            - Antecedência preferida  
            - Avaliação do profissional
            - Histórico de agendamentos
                    ↓
            [IA apresenta sugestão]
                    ↓
            {Cliente aprova?}
                ↓No              ↓Sim
        [Mostrar alternativas] [Confirmar agendamento]
                ↓                    ↓
        [Voltar para manual] [Processar automaticamente]
                                    ↓
                            [Retornar ao fluxo principal]
```

### Subfluxo: Validação de Conflitos

```
[Receber dados do agendamento]
    ↓
[Calcular tempo de início e fim]
    ↓
[Buscar agendamentos existentes do profissional]
    ↓
[Para cada agendamento existente:]
    ↓
{Há sobreposição de horário?}
    ↓Sim                    ↓Não
[Adicionar à lista]    [Continuar verificação]
de conflitos               ↓
    ↓                 [Próximo agendamento]
[Lista de conflitos]        ↓
    ↓                 {Fim da lista?}
{Lista vazia?}            ↓Não    ↓Sim
    ↓Não          ↓Sim         ↓         ↓
[Retornar erro] [Horário livre] ←─────────┘    └──→ [Continuar]
    ↓                ↓
[Fim com erro]   [Fim com sucesso]
```

### Subfluxo: Gestão de Status do Agendamento

```
[Agendamento criado com status "Agendado"]
    ↓
[Funcionário/Admin acessa agendamento]
    ↓
{Ação desejada?}
    ↓           ↓            ↓          ↓           ↓
[Confirmar] [Iniciar] [Finalizar] [Cancelar] [Não Compareceu]
    ↓           ↓            ↓          ↓           ↓
{Status atual [Status atual [Status atual [Qualquer] [Status atual
= Agendado?} = Confirmado?} = Em Andamento?}     = Confirmado?}
    ↓Sim        ↓Sim         ↓Sim        ↓Sim        ↓Sim
[Status →]  [Status →]   [Status →]  [Status →]  [Status →]
Confirmado  Em Andamento  Concluído   Cancelado   Não Compareceu
    ↓           ↓            ↓          ↓           ↓
    └─────────→ [Atualizar no banco] ←─────────────┘
                    ↓
            [Salvar timestamp]
                    ↓
            [Atualizar métricas do profissional]
                    ↓
            [Notificar cliente (se necessário)]
                    ↓
                [Fim]
```

### Subfluxo: Reagendamento

```
[Cliente/Funcionário seleciona agendamento]
    ↓
{Status permite reagendamento?}
(Agendado ou Confirmado)
    ↓Não                ↓Sim
[Exibir erro]      [Mostrar dados atuais]
    ↓                   ↓
[Fim]            [Permitir edição de data/hora]
                        ↓
                [Cliente/Funcionário altera]
                        ↓
                [Validar nova data/hora]
                        ↓
                {Data é futura?}
                    ↓Não          ↓Sim
                [Erro: data]   [Executar validação]
                passada        de conflitos
                    ↓               ↓
                [Voltar]        {Conflito?}
                              ↓Não      ↓Sim
                        [Atualizar]  [Mostrar erro]
                        agendamento      ↓
                            ↓        [Sugerir outros]
                        [Salvar]     horários
                            ↓            ↓
                        [Fim]        [Voltar]
```

### Subfluxo: Cancelamento

```
[Cliente/Funcionário seleciona agendamento]
    ↓
{Status permite cancelamento?}
(Não Concluído ou Não Cancelado)
    ↓Não                ↓Sim
[Exibir erro]      [Solicitar confirmação]
    ↓                   ↓
[Fim]            {Confirma cancelamento?}
                    ↓Não          ↓Sim
                [Voltar]      [Alterar status para]
                             "Cancelado"
                                 ↓
                            [Salvar no banco]
                                 ↓
                            [Registrar timestamp]
                                 ↓
                            [Liberar horário]
                                 ↓
                            [Notificar envolvidos]
                                 ↓
                                [Fim]
```

### Pontos de Decisão e Regras de Negócio

#### Validações Implementadas:
1. **Data/Hora futura**: appointmentDateTime > LocalDateTime.now()
2. **Conflito de horário**: Verificação de sobreposição com agendamentos existentes
3. **Profissional ativo**: Professional.status == ACTIVE
4. **Serviço ativo**: Service.status == ACTIVE
5. **Estabelecimento ativo**: Establishment.status == ACTIVE

#### Transições de Status Válidas:
- SCHEDULED → CONFIRMED
- SCHEDULED → CANCELLED
- CONFIRMED → IN_PROGRESS
- CONFIRMED → CANCELLED
- CONFIRMED → NO_SHOW
- IN_PROGRESS → COMPLETED
- IN_PROGRESS → CANCELLED

#### Permissões por Tipo de Usuário:
- **Cliente**: Criar, visualizar próprios, reagendar próprios, cancelar próprios
- **Funcionário**: Todas as operações do estabelecimento
- **Administrador**: Todas as operações + gestão de usuários

#### Algoritmo de Sugestão da IA:
1. Analisar intervalo preferido do cliente (semanal, quinzenal, mensal)
2. Considerar antecedência preferida (1 semana, 2 semanas, 1 mês)
3. Buscar profissional com melhor avaliação disponível
4. Encontrar horário baseado no perfil do cliente
5. Validar disponibilidade e ausência de conflitos
6. Apresentar sugestão formatada ao cliente

---

## 4. Resumo e Observações Técnicas

### Características do Sistema Slotfy

#### Arquitetura
- **Padrão**: MVC com Spring Boot
- **Camadas**: Controller → Service → Repository → Entity
- **Banco de Dados**: JPA/Hibernate com relacionamentos definidos
- **Frontend**: HTML/CSS/JavaScript com integração REST

#### Principais Funcionalidades Implementadas
1. **Sistema de Autenticação Dupla**: Clientes e Usuários de Estabelecimento
2. **Gestão Completa de Agendamentos**: CRUD com validações de negócio
3. **Sistema de Status**: Fluxo controlado de estados do agendamento
4. **Inteligência Artificial**: Sugestão automática de horários
5. **Validação de Conflitos**: Prevenção de sobreposição de agendamentos
6. **Multi-tenant**: Isolamento por estabelecimento

#### Regras de Negócio Críticas
- **RN001**: Não permitir agendamentos para datas passadas
- **RN002**: Não permitir sobreposição de agendamentos para o mesmo profissional
- **RN005**: Agendamentos devem ter cliente, profissional, serviço, estabelecimento e data/hora
- **RN006**: Transições de status seguem fluxo lógico

#### Pontos de Extensão Futura
1. **Notificações**: Email/SMS para clientes e profissionais
2. **Pagamentos**: Integração com gateways de pagamento
3. **Relatórios Avançados**: Analytics e dashboards mais detalhados
4. **App Mobile**: Aplicativo nativo para clientes
5. **API Pública**: Integração com sistemas externos

### Mapeamento Código → Diagramas

#### Casos de Uso → Controllers
- UC003 (Criar Agendamento) → AppointmentController.createAppointment()
- UC005 (Visualizar Agendamentos) → AppointmentController.getAppointments()
- UC006 (Gerenciar Status) → AppointmentController.updateStatus()
- UC007 (Reagendar) → AppointmentController.reschedule()

#### Classes → Entities
- Appointment ↔ Appointment.java
- Client ↔ Client.java
- Professional ↔ Professional.java
- Service ↔ Service.java
- Establishment ↔ Establishment.java

#### Atividades → Services
- Validação de Conflitos → AppointmentService.findConflictingAppointments()
- Criação de Agendamento → AppointmentService.createAppointment()
- Gestão de Status → AppointmentService.updateStatus()

### Correspondência com Requisitos Implementados

O sistema implementa **67 requisitos funcionais**, **42 não funcionais** e **46 regras de negócio**, que estão refletidos nos diagramas:

- **Casos de Uso**: Cobrem as principais jornadas do usuário
- **Classes**: Representam fielmente as entidades do código
- **Atividades**: Detalham os fluxos críticos do agendamento

### Notas para Conversão UML

Ao converter estes diagramas para UML visual, considere:

1. **Diagrama de Casos de Uso**: 
   - Use stereotypes para diferenciar atores
   - Agrupe casos de uso por módulo funcional
   - Destaque relacionamentos include/extend

2. **Diagrama de Classes**:
   - Use cores diferentes para entidades, enums e services
   - Mostre multiplicidade nos relacionamentos
   - Considere packages para organização

3. **Diagrama de Atividades**:
   - Use pools/lanes para separar responsabilidades
   - Destaque pontos de decisão com losangos
   - Use cores para diferenciar fluxos normais e de exceção

---

*Diagramas baseados na análise do código-fonte do sistema Slotfy*  
*Versão: Janeiro 2024*
