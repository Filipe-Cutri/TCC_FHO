# Railway Multi-Service Setup

## 🚨 Configuração Crítica

Este projeto usa **TRÊS serviços separados** no Railway:

1. **Backend Service** (Spring Boot)
2. **Frontend Service** (Static HTML/JS)
3. **PostgreSQL Database**

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
