# 🚀 Guia de Deploy - Slotfy no Railway

## 📋 Pré-requisitos

1. Conta no Railway.app
2. Repositório GitHub conectado ao Railway
3. Três serviços configurados:
   - Backend (Spring Boot)
   - Frontend (Static HTML/JS)
   - PostgreSQL Database
4. GitHub Secrets configurados (veja [GitHub Secrets Setup Guide](docs/GITHUB_SECRETS_SETUP.md))

## 🔧 Configuração Passo a Passo

### Opção 1: Configuração Automática via GitHub Actions (Recomendado)

1. Configure os GitHub Secrets (veja [GitHub Secrets Setup Guide](docs/GITHUB_SECRETS_SETUP.md)):
   - `RAILWAY_TOKEN`: Token de autenticação do Railway
   - `FRONTEND_URL`: URL do frontend (ex: `https://slotfy.com.br`)
   - `BACKEND_URL`: URL do backend (ex: `https://api.slotfy.com.br`)

2. Faça push para `main` ou execute manualmente o workflow de deploy

3. GitHub Actions automaticamente configurará as variáveis de ambiente nos serviços Railway

### Opção 2: Configuração Manual no Railway

### 1. Backend Service

#### Root Directory
```
back-end
```

#### Variáveis de Ambiente Obrigatórias

| Variável | Valor | Descrição |
|----------|-------|-----------|
| `FRONTEND_URL` | `https://slotfy.com.br` | **CRÍTICO**: URL do frontend para links em emails |
| `SPRING_PROFILES_ACTIVE` | `prod` | Perfil de produção |
| `DATABASE_URL` | (auto-configurado) | Railway configura automaticamente |

**⚠️ IMPORTANTE**: O `FRONTEND_URL` é usado para gerar links de recuperação de senha nos emails. **NUNCA** use `localhost` em produção!

**Como configurar no Railway:**
1. Acesse o Backend Service → Variables
2. Clique em "+ Add Variable"
3. Adicione: `FRONTEND_URL` = `https://slotfy.com.br` (use sua URL de produção)
4. Adicione: `SPRING_PROFILES_ACTIVE` = `prod`
5. Clique em "Deploy" para aplicar as mudanças

### 2. Frontend Service

#### Root Directory
```
front-end
```

#### Variáveis de Ambiente Obrigatórias

| Variável | Valor | Descrição |
|----------|-------|-----------|
| `BACKEND_URL` | `https://api.slotfy.com.br` | **CRÍTICO**: URL do backend para chamadas de API |

**Como configurar no Railway:**
1. Acesse o Frontend Service → Variables
2. Clique em "+ Add Variable"
3. Adicione: `BACKEND_URL` = `https://api.slotfy.com.br` (use sua URL de produção)
4. Clique em "Deploy" para aplicar as mudanças

**Nota**: O script `inject-config.sh` automaticamente injeta esta variável no arquivo `config.js` durante o build.

### 3. PostgreSQL Database

O Railway configura automaticamente:
- ✅ `DATABASE_URL` é injetado no Backend Service
- ✅ Credenciais são gerenciadas pelo Railway
- ✅ Backups automáticos

## 🔍 Verificação Pós-Deploy

### Backend

Teste o backend acessando:
```
https://seu-backend.railway.app/api/health
```

Deve retornar:
```json
{
  "status": "UP"
}
```

### Frontend

Acesse a URL do frontend:
```
https://seu-frontend.railway.app
```

Deve exibir a página inicial do Slotfy.

### Redirecionamento Automático

Acesse a raiz do backend:
```
https://seu-backend.railway.app/
```

Deve **redirecionar automaticamente** para a URL do frontend configurada em `FRONTEND_URL`.

## ⚠️ Problemas Comuns

### Problema 1: Recebo JSON ao acessar o backend
**Sintoma**: 
```json
{
  "application": "Slotfy Backend",
  "message": "Backend API está funcionando..."
}
```

**Solução**: 
1. Verifique que a variável `FRONTEND_URL` está configurada no Backend Service
2. Verifique que o valor NÃO contém `localhost`
3. Re-deploy do Backend após configurar a variável

### Problema 2: Frontend não consegue se conectar ao backend
**Sintoma**: Erros de rede nas chamadas de API

**Solução**:
1. Verifique que a variável `BACKEND_URL` está configurada no Frontend Service
2. Verifique que a URL do backend está correta (sem barra final)
3. Verifique os logs do build do Frontend para confirmar que `inject-config.sh` executou corretamente

### Problema 3: Links de email apontam para localhost
**Sintoma**: Emails de recuperação de senha contêm links para `https://localhost:8443`

**Solução**:
1. Configure a variável `FRONTEND_URL` no Backend Service
2. Certifique-se que o valor é a URL pública do frontend Railway
3. Re-deploy do Backend

### Problema 4: Vincular estabelecimento não funciona
**Sintoma**: Clicar no botão de vincular estabelecimento não faz nada

**Solução**:
Este problema foi corrigido no código. Certifique-se de usar a versão mais recente:
- Commit: `b33d12c` ou posterior
- Arquivo: `front-end/src/pages/client/client-establishments.html` (linha 153 corrigida)

## 🎯 Checklist de Deploy

Antes de considerar o deploy completo:

- [ ] Backend Service com `FRONTEND_URL` configurado
- [ ] Frontend Service com `BACKEND_URL` configurado
- [ ] PostgreSQL Database conectado
- [ ] Teste `/api/health` retorna sucesso
- [ ] Teste acesso ao frontend funciona
- [ ] Teste redirecionamento do backend root para frontend
- [ ] Teste login de cliente funciona
- [ ] Teste login de estabelecimento funciona
- [ ] Teste recuperação de senha (verifique link no email)
- [ ] Teste vinculação de estabelecimento

## 📚 Recursos Adicionais

- [Railway Setup](RAILWAY_SETUP.md) - Configuração detalhada de multi-serviços
- [Password Recovery Guide](docs/PASSWORD_RECOVERY_GUIDE.md) - Sistema de recuperação de senha
- [Deployment Corrections](CORREÇÕES_EMAIL.md) - Correções de email

## 🆘 Suporte

Se encontrar problemas:
1. Verifique os logs do Railway de cada serviço
2. Confirme que todas as variáveis de ambiente estão configuradas
3. Teste cada endpoint individualmente
4. Consulte a documentação adicional na pasta `docs/`
