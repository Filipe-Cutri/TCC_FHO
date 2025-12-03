# Railway Deployment Checklist

Use este checklist para garantir que sua aplicação está corretamente configurada para deploy no Railway.

## ✅ Pré-requisitos

- [ ] Conta no Railway criada
- [ ] Repositório GitHub conectado ao Railway
- [ ] Railway CLI instalado localmente (opcional, para testes)

## 📋 Backend Service

### Configuração Básica
- [ ] Serviço criado no Railway com nome "TCC_FHO: Back-end"
- [ ] Root directory configurado para: `back-end`
- [ ] Branch conectada: `main`

### Variáveis de Ambiente Obrigatórias
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `DATABASE_URL` (fornecido automaticamente ao conectar PostgreSQL)
- [ ] `SENDGRID_API_KEY` (sua chave da SendGrid)
- [ ] `SENDGRID_FROM` (email remetente, ex: noreply@slotfy.com)
- [ ] `FRONTEND_URL` (URL do frontend, ex: https://seu-frontend.railway.app)

### Variáveis de Ambiente Opcionais (AWS Bedrock)
- [ ] `AWS_REGION` (default: us-east-1)
- [ ] `AWS_ACCESS_KEY_ID`
- [ ] `AWS_SECRET_ACCESS_KEY`
- [ ] `BEDROCK_MODEL_ID` (default: meta.llama3-70b-instruct-v1:0)

### Database
- [ ] PostgreSQL database criado no Railway
- [ ] Database conectado ao backend service
- [ ] `DATABASE_URL` está presente nas variáveis de ambiente

### Arquivos de Configuração
- [ ] `back-end/nixpacks.toml` existe e está correto
- [ ] `back-end/src/main/resources/application-prod.properties` configurado
- [ ] `back-end/build.gradle` com dependências corretas

## 📋 Frontend Service

### Configuração Básica
- [ ] Serviço criado no Railway com nome "TCC_FHO: Front-end"
- [ ] Root directory configurado para: `front-end`
- [ ] Branch conectada: `main`

### Variáveis de Ambiente
- [ ] `BACKEND_URL` (URL do backend, ex: https://seu-backend.railway.app)
- [ ] `PORT` (fornecido automaticamente pelo Railway)

### Arquivos de Configuração
- [ ] `front-end/nixpacks.toml` existe e está correto
- [ ] `front-end/inject-config.sh` existe e é executável
- [ ] `front-end/src/config.js` existe (será sobrescrito no deploy)

## 🔄 GitHub Actions

### Secrets do GitHub
- [ ] `RAILWAY_TOKEN` adicionado aos secrets do repositório
  - Obtido em: Railway Dashboard → Account Settings → Tokens

### Workflow
- [ ] `.github/workflows/deploy.yml` está configurado
- [ ] Workflow tem jobs separados para backend e frontend
- [ ] Backend é deployado antes do frontend

## 🧪 Verificação Pós-Deploy

### Backend
- [ ] Serviço iniciou sem erros (verificar logs)
- [ ] Endpoint de health responde: `curl https://seu-backend.railway.app/api/health`
- [ ] Migrations Flyway executadas com sucesso
- [ ] Conexão com database estabelecida

### Frontend
- [ ] Serviço iniciou sem erros (verificar logs)
- [ ] Site carrega no browser: `https://seu-frontend.railway.app`
- [ ] Console do browser não mostra erros de CORS
- [ ] Frontend consegue fazer requisições ao backend

### Integração
- [ ] Login de cliente funciona
- [ ] Login de estabelecimento funciona
- [ ] Agendamentos podem ser criados
- [ ] Dados são persistidos no database

## 🐛 Troubleshooting

### Backend não inicia
1. Verificar logs no Railway
2. Confirmar que `SPRING_PROFILES_ACTIVE=prod`
3. Verificar que `DATABASE_URL` está presente
4. Testar build localmente: `cd back-end && ./gradlew clean build -x test`

### Frontend não conecta ao backend
1. Verificar que `BACKEND_URL` está configurado corretamente
2. Verificar logs do frontend no Railway
3. Abrir console do browser e verificar URL das requisições
4. Verificar CORS no backend

### Database connection error
1. Verificar que PostgreSQL está rodando
2. Verificar formato da `DATABASE_URL`
3. Testar conexão manual com `psql`

### Build falha
1. Verificar que root directory está correto
2. Verificar que `nixpacks.toml` existe
3. Verificar logs de build para erro específico

## 📚 Recursos Adicionais

- [Railway Documentation](https://docs.railway.app/)
- [Guia de Configuração do Railway](RAILWAY_CONFIGURATION.md)
- [Diferenças entre Localhost e Railway](LOCALHOST_VS_RAILWAY.md)

## 💡 Dicas

1. **Teste localmente primeiro**: Sempre teste o build com `-Pproduction` antes de fazer deploy
2. **Variáveis de ambiente**: Use valores de teste/desenvolvimento nas variáveis enquanto configura
3. **Logs são seus amigos**: Sempre verifique os logs quando algo não funcionar
4. **Incremental**: Configure e teste um serviço por vez (backend primeiro, depois frontend)
5. **Rollback**: Railway mantém deployments anteriores, você pode fazer rollback se necessário
