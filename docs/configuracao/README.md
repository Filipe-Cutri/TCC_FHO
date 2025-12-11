# Configuração - Variáveis de Ambiente e Secrets

## Visão Geral

O sistema utiliza variáveis de ambiente para configuração, seguindo as práticas de aplicações cloud-native (12-factor app).

## Variáveis do Backend

### Obrigatórias em Produção

#### Banco de Dados
```properties
# Railway injeta automaticamente:
DATABASE_URL=postgresql://user:password@host:port/database
# ou
JDBC_DATABASE_URL=jdbc:postgresql://host:port/database?user=user&password=password
```

#### URLs
```properties
FRONTEND_URL=https://slotfy.com.br
# URL do frontend para:
# - Links de reset de senha em e-mails
# - Redirecionamento da raiz do backend
```

### Opcionais (com valores padrão)

#### Perfil do Spring
```properties
SPRING_PROFILES_ACTIVE=prod
# Valores: dev, prod, test
# Padrão: dev
```

#### AWS Bedrock (IA)
```properties
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=meta.llama3-70b-instruct-v1:0
AWS_ACCESS_KEY_ID=sua_access_key
AWS_SECRET_ACCESS_KEY=sua_secret_key
```

#### Versão da Aplicação
```properties
APP_VERSION=v1.0.0
COMMIT_HASH=abc1234
BUILD_DATE=2024-01-01T12:00:00Z
# Injetadas automaticamente pelo GitHub Actions
```

### E-mail (Gmail SMTP)

Configuradas em `application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=seu_email@gmail.com
spring.mail.password=sua_app_password

# App Password do Gmail (não é a senha normal)
# Gerar em: https://myaccount.google.com/apppasswords
```

## Variáveis do Frontend

### Obrigatórias em Produção

```bash
BACKEND_URL=https://api.slotfy.com.br
```

Esta variável é injetada durante o build pelo script `inject-config.sh` e gera o arquivo `/config.js`:

```javascript
window.BACKEND_URL = 'https://api.slotfy.com.br';
```

### Versão da Aplicação (opcionais)
```bash
APP_VERSION=v1.0.0
COMMIT_HASH=abc1234
BUILD_DATE=2024-01-01T12:00:00Z
```

## GitHub Secrets

Configure os seguintes secrets no GitHub (Settings → Secrets and variables → Actions):

### Secrets Necessários

| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `RAILWAY_TOKEN` | Token de API do Railway | `eyJ...` |
| `SONAR_TOKEN` | Token do SonarCloud | `sqp_...` |
| `CODECOV_TOKEN` | Token do Codecov | `abc123...` |
| `FRONTEND_URL` | URL do frontend em produção | `https://slotfy.com.br` |
| `BACKEND_URL` | URL da API em produção | `https://api.slotfy.com.br` |

### Como Obter os Tokens

#### RAILWAY_TOKEN
1. Acesse [Railway Dashboard](https://railway.app)
2. Clique no seu avatar → Account Settings
3. Vá em "Tokens"
4. Clique em "Create New Token"
5. Copie o token gerado

#### SONAR_TOKEN
1. Acesse [SonarCloud](https://sonarcloud.io)
2. Faça login
3. Clique no seu avatar → My Account
4. Vá em "Security"
5. Em "Generate Tokens", dê um nome e clique em "Generate"
6. Copie o token gerado

#### CODECOV_TOKEN
1. Acesse [Codecov](https://codecov.io)
2. Faça login com GitHub
3. Selecione seu repositório
4. Vá em Settings
5. Copie o "Repository Upload Token"

### Como Adicionar Secrets no GitHub

1. Acesse seu repositório no GitHub
2. Vá em **Settings**
3. No menu lateral, clique em **Secrets and variables** → **Actions**
4. Clique em **New repository secret**
5. Digite o nome do secret (ex: `RAILWAY_TOKEN`)
6. Cole o valor
7. Clique em **Add secret**

## Configuração no Railway

### Backend Service

**Variáveis configuradas automaticamente pelo GitHub Actions:**
- `APP_VERSION`
- `COMMIT_HASH`
- `BUILD_DATE`
- `FRONTEND_URL`

**Variáveis configuradas manualmente:**
- `SPRING_PROFILES_ACTIVE=prod`
- AWS credentials (se usar Bedrock)
- E-mail credentials (se não estiver em application.properties)

**Variáveis injetadas pelo Railway:**
- `DATABASE_URL` (quando você adiciona um PostgreSQL)

### Frontend Service

**Variáveis configuradas automaticamente pelo GitHub Actions:**
- `APP_VERSION`
- `COMMIT_HASH`
- `BUILD_DATE`
- `BACKEND_URL`

### Como Configurar Variáveis Manualmente no Railway

1. Acesse [Railway Dashboard](https://railway.app)
2. Selecione seu projeto
3. Clique no serviço (backend ou frontend)
4. Vá na aba **Variables**
5. Clique em **+ New Variable**
6. Digite o nome e valor
7. As variáveis são aplicadas no próximo deploy

## Perfis do Spring Boot

### development (dev)
Usado para desenvolvimento local.

```properties
spring.profiles.active=dev
```

### production (prod)
Usado em produção (Railway).

```properties
spring.profiles.active=prod
```

### test
Usado para testes automatizados.

```properties
spring.profiles.active=test
```

## Configurações Importantes

### CORS

Backend configurado para aceitar requisições do frontend:

```java
// back-end/src/main/java/com/slotfy/config/WebConfig.java
allowedOrigins = System.getenv("FRONTEND_URL")
```

### Security

Spring Security desabilitado para a maioria dos endpoints (projeto em desenvolvimento).

### File Upload

Limite de upload de arquivos:
```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

## Troubleshooting

### Backend não conecta no banco
- Verifique se `DATABASE_URL` está configurada no Railway
- Verifique logs do Railway para erros de conexão

### Frontend não consegue chamar API
- Verifique se `BACKEND_URL` está configurado no Railway
- Verifique se `inject-config.sh` foi executado durante o build
- Verifique se `/config.js` é carregado antes de `api-config.js`

### E-mails não são enviados
- Verifique se você está usando App Password do Gmail (não a senha normal)
- Verifique se as configurações SMTP estão corretas
- Verifique logs do backend para erros

### GitHub Actions falha ao configurar variáveis
- Verifique se `RAILWAY_TOKEN` está configurado nos Secrets
- Verifique se os nomes dos serviços no Railway são exatamente "backend" e "frontend"
- Verifique logs do workflow para mensagens de erro
