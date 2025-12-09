# Railway Multi-Service Setup

## 🚨 Configuração Crítica

Este projeto usa **TRÊS serviços separados** no Railway:

1. **Backend Service** (Spring Boot)
2. **Frontend Service** (Static HTML/JS)
3. **PostgreSQL Database**

## 🔐 Variáveis de Ambiente Obrigatórias

### Backend Service

As seguintes variáveis de ambiente **DEVEM** ser configuradas no Railway:

1. **FRONTEND_URL** (Obrigatório)
   - URL do frontend para links em emails de recuperação de senha
   - Exemplo: `https://tcc-fho-production.up.railway.app`
   - ⚠️ **NUNCA** use `localhost` em produção
   - Esta variável é usada em `application.properties` como `frontend.url=${FRONTEND_URL:https://localhost:8443}`

2. **DATABASE_URL** (Configurado automaticamente pelo Railway)
   - URL de conexão com o PostgreSQL

3. **SPRING_PROFILES_ACTIVE** (Opcional, padrão: `dev`)
   - Recomendado: `prod` para produção

### Frontend Service

1. **BACKEND_URL** (Obrigatório)
   - URL do backend para chamadas de API
   - Exemplo: `https://tcc-fho-backend-production.up.railway.app`
   - Esta variável é injetada no `config.js` durante o build via `inject-config.sh`

## ⚠️ IMPORTANTE: Estrutura de Arquivos

### ✅ CORRETO

```
TCC_FHO/
├── back-end/
│   ├── nixpacks.toml          ✅ Configuração do backend
│   ├── build.gradle
│   └── src/
├── front-end/
│   ├── nixpacks.toml          ✅ Configuração do frontend
│   ├── inject-config.sh
│   └── src/
└── README.md
```

### ❌ ERRADO

```
TCC_FHO/
├── nixpacks.toml              ❌ NÃO DEVE EXISTIR NA RAIZ!
├── back-end/
│   ├── nixpacks.toml          ✅
│   └── ...
├── front-end/
│   ├── nixpacks.toml          ✅
│   └── ...
```

## 📋 Configuração no Railway

### Backend Service

**Settings → Root Directory:**
```
back-end
```

**Usa automaticamente:** `back-end/nixpacks.toml`

### Frontend Service

**Settings → Root Directory:**
```
front-end
```

**Usa automaticamente:** `front-end/nixpacks.toml`

## 🔧 Por que isso é importante?

- **Com root `nixpacks.toml`:** Railway pode tentar construir tudo a partir da raiz, ignorando as configurações específicas de cada serviço
- **Sem root `nixpacks.toml`:** Railway usa a configuração "Root Directory" de cada serviço e encontra o `nixpacks.toml` correto automaticamente

## 📚 Documentação Completa

Para mais detalhes, consulte:
- [Guia de Configuração do Railway](docs/deployment/RAILWAY_CONFIGURATION.md)
- [Railway Deployment Checklist](docs/deployment/RAILWAY_DEPLOYMENT_CHECKLIST.md)
- [Diferenças entre Localhost e Railway](docs/deployment/LOCALHOST_VS_RAILWAY.md)

## 🐛 Solução de Problemas

Se o Railway não está construindo corretamente:

1. ✅ Verifique que NÃO existe `nixpacks.toml` na raiz
2. ✅ Verifique que existe `back-end/nixpacks.toml`
3. ✅ Verifique que existe `front-end/nixpacks.toml`
4. ✅ Verifique a configuração "Root Directory" em cada serviço Railway
5. ✅ Verifique os logs de build do Railway para mensagens de erro específicas

## 💡 Deploy Automático

O GitHub Actions (`.github/workflows/deploy.yml`) gerencia o deploy automático:
- Backend e Frontend são deployados como serviços separados
- Variáveis de ambiente são configuradas automaticamente
- Cada serviço é construído a partir de seu próprio diretório raiz
