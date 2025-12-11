# Arquitetura do Sistema

## Visão Geral

O Slotfy é uma aplicação web full-stack com arquitetura baseada em **backend REST API** e **frontend SPA** (Single Page Application).

## Arquitetura de Alto Nível

```
┌─────────────────┐
│   Navegador     │
│   (Cliente)     │
└────────┬────────┘
         │ HTTPS
         ▼
┌─────────────────┐
│   Frontend      │  HTML/CSS/JavaScript
│   (SPA)         │  Vanilla JS
└────────┬────────┘
         │ REST API
         │ (JSON)
         ▼
┌─────────────────┐
│   Backend       │  Spring Boot 3
│   (REST API)    │  Java 17
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│   PostgreSQL    │
│   (Database)    │
└─────────────────┘
```

## Backend - Spring Boot

### Camadas da Aplicação

#### 1. Controller Layer (Endpoints REST)
Localização: `back-end/src/main/java/com/slotfy/controller/`

Responsável por:
- Receber requisições HTTP
- Validar entrada de dados
- Chamar serviços
- Retornar respostas HTTP

**Principais Controllers:**
- `AppointmentController`: Agendamentos
- `ClientController`: Clientes
- `EstablishmentController`: Estabelecimentos
- `ProfessionalController`: Profissionais
- `ServiceController`: Serviços
- `EstablishmentUserController`: Usuários do estabelecimento
- `ForgotPasswordController`: Recuperação de senha

#### 2. Service Layer (Lógica de Negócio)
Localização: `back-end/src/main/java/com/slotfy/service/`

Responsável por:
- Implementar regras de negócio
- Coordenar operações
- Processar dados

**Principais Services:**
- `AppointmentService`: Lógica de agendamentos
- `ClientService`: Gestão de clientes
- `EstablishmentService`: Gestão de estabelecimentos
- `EmailService`: Envio de e-mails
- `ForgotPasswordService`: Recuperação de senha
- `ImageUploadService`: Upload de imagens (AWS S3)

#### 3. Repository Layer (Acesso a Dados)
Localização: `back-end/src/main/java/com/slotfy/repository/`

Responsável por:
- Acesso ao banco de dados
- Queries personalizadas
- CRUD básico (via Spring Data JPA)

**Principais Repositories:**
- `AppointmentRepository`
- `ClientRepository`
- `EstablishmentRepository`
- `ProfessionalRepository`
- `ServiceRepository`

#### 4. Model Layer (Entidades)
Localização: `back-end/src/main/java/com/slotfy/model/`

Entidades JPA que mapeiam as tabelas do banco:
- `Appointment`
- `Client`
- `Establishment`
- `Professional`
- `Service`
- `EstablishmentUser`

#### 5. DTO Layer (Data Transfer Objects)
Localização: `back-end/src/main/java/com/slotfy/dto/`

Objetos para transferência de dados entre camadas.

#### 6. Configuration Layer
Localização: `back-end/src/main/java/com/slotfy/config/`

Configurações do Spring:
- `SecurityConfig`: Configuração de segurança
- `WebConfig`: Configuração CORS
- `BedrockConfig`: Configuração AWS Bedrock (IA)

### Segurança

- Spring Security para autenticação
- Senhas criptografadas (BCrypt)
- CORS configurado para permitir frontend
- Validação de entrada de dados

### Integrações Externas

#### AWS Bedrock
Integração com IA da AWS para funcionalidades inteligentes.

**Configuração:**
```properties
aws.region=us-east-1
bedrock.model.id=meta.llama3-70b-instruct-v1:0
```

#### E-mail (Gmail SMTP)
Envio de e-mails para recuperação de senha.

**Configuração:**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.properties.mail.smtp.ssl.enable=true
```

#### AWS S3
Upload de imagens de serviços e profissionais.

## Frontend - SPA Vanilla JavaScript

### Estrutura

```
front-end/src/
├── index.html              # Landing page
├── pages/                  # Páginas da aplicação
│   ├── client/            # Área do cliente
│   ├── establishment/     # Área do estabelecimento
│   ├── reset-password.html
│   └── ...
├── assets/                # Recursos estáticos
│   ├── css/              # Estilos
│   ├── js/               # JavaScript
│   │   ├── api-config.js      # Configuração da API
│   │   ├── client-session.js  # Sessão do cliente
│   │   └── ...
│   └── images/           # Imagens, ícones, logos
└── config.js             # Configuração dinâmica (gerada pelo inject-config.sh)
```

### Características

- **SPA**: Aplicação de página única com navegação client-side
- **Vanilla JS**: Sem frameworks (React, Vue, etc.)
- **Responsivo**: Design adaptável para mobile e desktop
- **PWA Ready**: Manifest e service workers configurados

### Comunicação com Backend

Todas as páginas carregam:
1. `/config.js` - Configuração dinâmica do `BACKEND_URL`
2. `api-config.js` - Configuração da API que usa `window.BACKEND_URL`

**Ordem de carregamento (importante):**
```html
<script src="/config.js"></script>  <!-- PRIMEIRO -->
<script src="/assets/js/api-config.js"></script>  <!-- DEPOIS -->
```

### Gestão de Sessão

- `client-session.js`: Gerencia autenticação de clientes
- `establishment-session.js`: Gerencia autenticação de estabelecimentos
- LocalStorage para persistência de tokens/sessão

## Deploy - Arquitetura Multi-Serviço

### Railway

O sistema é deployado como **dois serviços independentes** no Railway:

#### 1. Backend Service
- **Root Directory**: `back-end/`
- **Build**: Gradle → JAR
- **Run**: `java -jar slotify-backend-*.jar`
- **Variáveis**:
  - `DATABASE_URL` (auto-injetado pelo Railway)
  - `FRONTEND_URL` (configurado via GitHub Secrets)
  - AWS credentials, email config, etc.

#### 2. Frontend Service
- **Root Directory**: `front-end/`
- **Build**: `inject-config.sh` injeta `BACKEND_URL`
- **Run**: `serve -s src`
- **Variáveis**:
  - `BACKEND_URL` (configurado via GitHub Secrets)

### Configuração de Ambiente

Cada serviço tem seu próprio `nixpacks.toml`:
- `back-end/nixpacks.toml`: Configuração do backend
- `front-end/nixpacks.toml`: Configuração do frontend

**Importante:** Não deve existir `nixpacks.toml` na raiz do repositório (conflito multi-serviço).

## Fluxo de Dados (Exemplo: Criar Agendamento)

```
1. Cliente preenche formulário no frontend
   ↓
2. JavaScript faz POST para /api/appointments
   ↓
3. AppointmentController recebe requisição
   ↓
4. Controller valida dados e chama AppointmentService
   ↓
5. Service aplica regras de negócio (disponibilidade, etc.)
   ↓
6. Service chama Repository para salvar no banco
   ↓
7. Repository executa INSERT no PostgreSQL
   ↓
8. Resposta retorna pela pilha até o frontend
   ↓
9. Frontend exibe confirmação para o cliente
```

## Princípios Arquiteturais

### Backend
- **Separação de Responsabilidades**: Cada camada tem uma função clara
- **Dependency Injection**: Spring gerencia dependências
- **RESTful API**: Endpoints seguem padrões REST
- **Stateless**: Backend não mantém estado de sessão

### Frontend
- **Progressive Enhancement**: Funciona sem JavaScript avançado
- **Separation of Concerns**: HTML (estrutura), CSS (apresentação), JS (comportamento)
- **API-First**: Frontend é consumidor da API

### Geral
- **Twelve-Factor App**: Seguindo princípios de aplicações cloud-native
- **Environment-based Config**: Configurações via variáveis de ambiente
- **Automated Deployment**: CI/CD totalmente automatizado
