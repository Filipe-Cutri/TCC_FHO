# Correção: Configuração Multi-Service do Railway

## 📋 Resumo da Correção

**Data:** 2025-12-09  
**Issue:** Conflito entre `nixpacks.toml` na raiz do repositório e arquitetura multi-service do Railway

## 🔍 Problema Identificado

O usuário reportou ter 3 serviços separados no Railway (backend, frontend e PostgreSQL), mas o repositório continha um arquivo `nixpacks.toml` na raiz que estava causando conflitos.

### Estrutura Problemática (ANTES)

```
TCC_FHO/
├── nixpacks.toml              ❌ CONFLITO - arquivo na raiz
│   └── Tentava construir de: back-end/ (navegação relativa)
├── back-end/
│   └── nixpacks.toml          ✅ Configuração correta do backend
└── front-end/
    └── nixpacks.toml          ✅ Configuração correta do frontend
```

### Consequências do Problema

1. **Ambiguidade:** Railway poderia detectar o `nixpacks.toml` da raiz em vez dos arquivos específicos
2. **Build incorreto:** O arquivo raiz navegava para `back-end/` mas não funcionava corretamente com multi-service
3. **Confusão:** Desenvolvedores não sabiam qual arquivo Railway estava usando
4. **Incompatível:** Não seguia a arquitetura documentada de serviços separados

## ✅ Solução Implementada

### 1. Removido arquivo conflitante

**Arquivo removido:** `/nixpacks.toml` (raiz do repositório)

**Razão:** Com a configuração multi-service do Railway, cada serviço deve usar seu próprio `nixpacks.toml`:
- Backend Service (root directory: `back-end`) → usa `back-end/nixpacks.toml`
- Frontend Service (root directory: `front-end`) → usa `front-end/nixpacks.toml`

### 2. Estrutura Correta (DEPOIS)

```
TCC_FHO/
├── back-end/
│   ├── nixpacks.toml          ✅ Backend: openjdk17, Gradle build
│   ├── build.gradle
│   └── src/
├── front-end/
│   ├── nixpacks.toml          ✅ Frontend: Node.js, serve
│   ├── inject-config.sh
│   └── src/
└── RAILWAY_SETUP.md           ✅ Nova documentação
```

### 3. Documentação Atualizada

#### Novos/Atualizados Arquivos

1. **`RAILWAY_SETUP.md`** (NOVO)
   - Explicação visual da estrutura correta vs incorreta
   - Guia rápido de configuração
   - Troubleshooting específico

2. **`docs/deployment/RAILWAY_CONFIGURATION.md`** (ATUALIZADO)
   - Adicionado aviso crítico sobre nixpacks.toml na raiz
   - Seção de troubleshooting expandida
   - Notas sobre configuração multi-service

3. **`docs/deployment/RAILWAY_DEPLOYMENT_CHECKLIST.md`** (ATUALIZADO)
   - Adicionado checklist para verificar ausência de nixpacks.toml na raiz
   - Troubleshooting de build atualizado

## 🎯 Configuração Correta do Railway

### Backend Service

```yaml
Service Name: backend (ou "TCC_FHO: Back-end")
Root Directory: back-end
Build: Detecta back-end/nixpacks.toml automaticamente

Variáveis de Ambiente Obrigatórias:
- SPRING_PROFILES_ACTIVE=prod
- DATABASE_URL (automático ao conectar PostgreSQL)
- SENDGRID_API_KEY
- SENDGRID_FROM
- FRONTEND_URL
- APP_VERSION (via GitHub Actions)
- COMMIT_HASH (via GitHub Actions)
```

### Frontend Service

```yaml
Service Name: frontend (ou "TCC_FHO: Front-end")
Root Directory: front-end
Build: Detecta front-end/nixpacks.toml automaticamente

Variáveis de Ambiente Obrigatórias:
- BACKEND_URL (URL do backend service)
- PORT (automático)
- APP_VERSION (via GitHub Actions)
- COMMIT_HASH (via GitHub Actions)
```

### PostgreSQL Database

```yaml
Service Name: postgres (ou qualquer nome)
Conectado ao: Backend Service
Fornece: DATABASE_URL automaticamente
```

## 🔄 Como Railway Funciona Agora

### Processo de Build

1. **Backend Service:**
   ```
   Railway detecta root directory = back-end/
   → Procura back-end/nixpacks.toml
   → Executa: ./gradlew clean build -x test
   → Inicia: java -jar build/libs/slotify-backend-*.jar
   ```

2. **Frontend Service:**
   ```
   Railway detecta root directory = front-end/
   → Procura front-end/nixpacks.toml
   → Executa: inject-config.sh (injeta BACKEND_URL)
   → Inicia: serve -s src -l $PORT
   ```

### GitHub Actions Deploy

```yaml
workflow: deploy.yml
→ Extrai versão do git tag
→ Define APP_VERSION, COMMIT_HASH para backend
→ Define APP_VERSION, COMMIT_HASH para frontend
→ railway up --service backend
→ railway up --service frontend
```

## ✅ Verificação da Correção

### Checklist de Validação

- [x] Arquivo `nixpacks.toml` removido da raiz
- [x] `back-end/nixpacks.toml` existe e está correto
- [x] `front-end/nixpacks.toml` existe e está correto
- [x] Documentação atualizada com avisos críticos
- [x] RAILWAY_SETUP.md criado com explicação visual
- [x] Checklist de deployment atualizado

### Como Testar

1. **Verificar estrutura de arquivos:**
   ```bash
   find . -name "nixpacks.toml"
   # Deve retornar apenas:
   # ./back-end/nixpacks.toml
   # ./front-end/nixpacks.toml
   ```

2. **No Railway Dashboard:**
   - Backend Service → Settings → Root Directory = `back-end`
   - Frontend Service → Settings → Root Directory = `front-end`

3. **Após deploy:**
   ```bash
   # Backend health
   curl https://seu-backend.railway.app/api/health
   
   # Frontend version
   curl https://seu-frontend.railway.app/version.json
   ```

## 📚 Recursos de Referência

- **Quick Start:** `RAILWAY_SETUP.md`
- **Configuração Completa:** `docs/deployment/RAILWAY_CONFIGURATION.md`
- **Checklist:** `docs/deployment/RAILWAY_DEPLOYMENT_CHECKLIST.md`
- **Workflow:** `.github/workflows/deploy.yml`

## 💡 Prevenção de Problemas Futuros

### ⚠️ NUNCA faça:

- ❌ Criar `nixpacks.toml` na raiz do repositório
- ❌ Configurar Railway sem definir Root Directory
- ❌ Ter múltiplos serviços apontando para o mesmo diretório

### ✅ SEMPRE faça:

- ✅ Use um `nixpacks.toml` para cada serviço (dentro do seu diretório)
- ✅ Configure Root Directory em cada serviço Railway
- ✅ Teste localmente antes de fazer deploy
- ✅ Verifique logs do Railway após deploy

## 🎉 Resultado Final

Com esta correção:
- ✅ Backend e Frontend são construídos independentemente
- ✅ Cada serviço usa sua própria configuração
- ✅ Deploy automático via GitHub Actions funciona corretamente
- ✅ Documentação clara previne erros futuros
- ✅ Arquitetura multi-service totalmente funcional
