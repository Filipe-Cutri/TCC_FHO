# Deploy no Railway

## Visão Geral

O sistema é deployado no Railway como **dois serviços independentes**:
- **Backend**: API REST (Spring Boot)
- **Frontend**: Servidor de arquivos estáticos (Vanilla JS)

## Pré-requisitos

1. Conta no [Railway](https://railway.app)
2. Repositório conectado ao Railway
3. Secrets configurados no GitHub (ver [Configuração](../configuracao/README.md))

## Configuração Inicial no Railway

### 1. Criar Projeto no Railway

1. Acesse [Railway Dashboard](https://railway.app)
2. Clique em **New Project**
3. Selecione **Deploy from GitHub repo**
4. Escolha o repositório `Filipe-Cutri/TCC_FHO`

### 2. Adicionar PostgreSQL

1. No projeto criado, clique em **+ New**
2. Selecione **Database** → **PostgreSQL**
3. O Railway criará um banco de dados e injetará automaticamente a variável `DATABASE_URL`

### 3. Configurar Serviço Backend

1. Clique em **+ New** → **GitHub Repo**
2. Nas configurações do serviço:
   - **Service Name**: `backend`
   - **Root Directory**: `back-end`
   - O Railway detectará automaticamente o `nixpacks.toml`

3. Configurar variáveis de ambiente:
   - Vá na aba **Variables**
   - Adicione:
     ```
     SPRING_PROFILES_ACTIVE=prod
     FRONTEND_URL=https://slotfy.com.br
     ```
   - `DATABASE_URL` já está configurada automaticamente

4. Conectar ao PostgreSQL:
   - Na aba **Settings**, em **Service Connections**
   - Clique em **+ New Connection**
   - Selecione o PostgreSQL criado anteriormente

### 4. Configurar Serviço Frontend

1. Clique em **+ New** → **GitHub Repo**
2. Nas configurações do serviço:
   - **Service Name**: `frontend`
   - **Root Directory**: `front-end`
   - O Railway detectará automaticamente o `nixpacks.toml`

3. Configurar variáveis de ambiente:
   - Vá na aba **Variables**
   - Adicione:
     ```
     BACKEND_URL=https://api.slotfy.com.br
     ```
   - (Substitua pela URL real do backend após o primeiro deploy)

### 5. Configurar Domínios Customizados

#### Backend:
1. No serviço backend, vá em **Settings**
2. Em **Networking** → **Public Networking**
3. Clique em **Generate Domain** (Railway gera um domínio .up.railway.app)
4. Ou configure domínio customizado:
   - Clique em **Custom Domain**
   - Digite `api.slotfy.com.br`
   - Adicione os registros DNS conforme instruções do Railway

#### Frontend:
1. No serviço frontend, vá em **Settings**
2. Em **Networking** → **Public Networking**
3. Configure domínio customizado:
   - Clique em **Custom Domain**
   - Digite `slotfy.com.br`
   - Adicione os registros DNS

## Deploy Automático via GitHub Actions

### Configuração

1. Obtenha o Railway Token:
   - Railway Dashboard → Account Settings → Tokens
   - Clique em "Create New Token"
   - Copie o token

2. Adicione o token nos GitHub Secrets:
   - GitHub Repo → Settings → Secrets and variables → Actions
   - New repository secret:
     - Name: `RAILWAY_TOKEN`
     - Value: (cole o token)

3. Configure URLs nos Secrets:
   - `FRONTEND_URL`: `https://slotfy.com.br`
   - `BACKEND_URL`: `https://api.slotfy.com.br`

### Processo de Deploy

O deploy automático ocorre quando:
1. **Push na branch `main`**
2. **Tag de release** (formato `v*.*.*`)
3. **Manualmente** via workflow_dispatch

**Passos do deploy:**
1. GitHub Actions captura versão, commit e data
2. Configura variáveis de ambiente no Railway (backend e frontend)
3. Faz trigger do redeploy nos dois serviços

## Deploy Manual

### Via Railway CLI

```bash
# Instalar Railway CLI
npm install -g @railway/cli

# Login
railway login

# Link ao projeto
railway link

# Deploy backend
railway up --service backend

# Deploy frontend
railway up --service frontend
```

### Via Git Push

O Railway redeploy automaticamente quando há push na branch configurada (geralmente `main`).

## Verificar Deploy

### 1. Logs do Railway

- Acesse o serviço no Railway Dashboard
- Vá na aba **Deployments**
- Clique no último deployment
- Veja os **Build Logs** e **Deploy Logs**

### 2. Verificar Backend

```bash
curl https://api.slotfy.com.br/
# Deve retornar JSON com informações da API
```

### 3. Verificar Frontend

Acesse `https://slotfy.com.br` no navegador.

### 4. Verificar Conexão Backend-Frontend

1. Abra o frontend
2. Abra DevTools (F12)
3. Vá na aba **Console**
4. Digite: `window.BACKEND_URL`
5. Deve mostrar a URL correta da API

## Estrutura de Arquivos Importante

### Backend: `back-end/nixpacks.toml`
```toml
[phases.setup]
nixPkgs = ['openjdk17']

[phases.build]
cmds = ['./gradlew clean build -x test']

[start]
cmd = 'java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar build/libs/slotify-backend-*.jar'
```

### Frontend: `front-end/nixpacks.toml`
```toml
[phases.setup]
nixPkgs = ['nodejs', 'bash']

[phases.install]
cmds = ['npm install -g serve']

[phases.build]
cmds = ['chmod +x inject-config.sh && ./inject-config.sh']

[start]
cmd = 'serve -s src -l $PORT'
```

### Frontend: `front-end/inject-config.sh`
Script que injeta `BACKEND_URL` durante o build, gerando `/config.js`.

## Troubleshooting

### Backend não inicia
- Verifique logs no Railway
- Confirme que `DATABASE_URL` está configurada
- Verifique se o JAR foi buildado corretamente
- Confirme Java 17 está sendo usado

### Frontend mostra erro de API
- Verifique se `BACKEND_URL` está configurado
- Confirme que `inject-config.sh` foi executado (veja logs de build)
- Verifique se `/config.js` existe e tem a URL correta
- Verifique CORS no backend

### Database connection error
- Verifique se o serviço backend está conectado ao PostgreSQL
- Verifique se `DATABASE_URL` tem o formato correto
- Verifique se o Postgres está rodando

### Deploy não é triggered
- Verifique se `RAILWAY_TOKEN` está nos GitHub Secrets
- Verifique se os nomes dos serviços são exatamente "backend" e "frontend"
- Verifique logs do GitHub Actions workflow

## Diferenças entre Localhost e Railway

| Aspecto | Localhost | Railway |
|---------|-----------|---------|
| **Backend URL** | `https://localhost:8443` | `https://api.slotfy.com.br` |
| **Frontend URL** | `http://localhost:3000` | `https://slotfy.com.br` |
| **Database** | PostgreSQL local | PostgreSQL do Railway |
| **HTTPS** | Self-signed cert | Certificate válido |
| **Variáveis** | `.env` ou IDE | Railway Variables |
| **Build** | Manual (IDE/Gradle) | Automático (nixpacks) |

## Rollback

Para reverter um deploy:

1. No Railway Dashboard, vá no serviço
2. Aba **Deployments**
3. Encontre o deployment anterior que funcionava
4. Clique nos três pontos → **Redeploy**

Ou via CLI:
```bash
railway rollback --service backend
railway rollback --service frontend
```

## Monitoramento

### Métricas do Railway
- CPU usage
- Memory usage
- Network traffic
- Deployment history

### Logs
```bash
# Ver logs em tempo real
railway logs --service backend
railway logs --service frontend
```

## Custos

Railway oferece:
- **Plano Gratuito**: $5 de crédito/mês
- **Developer Plan**: $5/mês + uso
- **Team Plan**: $20/mês + uso

PostgreSQL e dois serviços consomem aproximadamente:
- ~$3-5/mês no plano gratuito (suficiente para desenvolvimento/demonstração)
