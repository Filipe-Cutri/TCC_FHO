# Railway Deployment Configuration

Este documento descreve a configuração necessária para o deploy automático no Railway através do GitHub Actions.

## Visão Geral

O workflow de release (`release.yml`) agora inclui uma job `deploy-to-railway` que faz o deploy automático da aplicação Spring Boot para o Railway após a criação bem-sucedida de uma release.

## Pré-requisitos

### 1. Secrets do GitHub

Os seguintes secrets devem estar configurados no repositório GitHub:

#### `RAILWAY_TOKEN`
- **Descrição**: Token de API do Railway para autenticação
- **Como obter**: 
  1. Acesse o [Railway Dashboard](https://railway.app/)
  2. Vá em Settings → Account → Tokens
  3. Clique em "Generate new token"
  4. Copie o token gerado
  
**Valor atual fornecido**: `f4952abc-9f7c-449d-9920-d7997282d81a`

#### `RAILWAY_SERVICE_ID`
- **Descrição**: ID único do serviço backend no Railway
- **Como obter**:
  1. Acesse seu projeto no [Railway Dashboard](https://railway.app/)
  2. Selecione o serviço do backend (Spring Boot)
  3. Vá em Settings
  4. Copie o Service ID (encontrado na URL ou nas configurações do serviço)

### 2. Como Configurar os Secrets no GitHub

1. Vá até o repositório no GitHub
2. Clique em **Settings** → **Secrets and variables** → **Actions**
3. Clique em **New repository secret**
4. Adicione cada secret:
   - Nome: `RAILWAY_TOKEN`
   - Valor: (cole o token do Railway)
   - Clique em **Add secret**
   
   Repita para `RAILWAY_SERVICE_ID`

## Como Funciona

### Workflow de Deployment

O deploy é acionado automaticamente quando:

1. ✅ Um Pull Request é **merged** para a branch `main`
2. ✅ O PR **não é um draft**
3. ✅ A job `create-release` é executada com sucesso
4. ✅ Uma nova tag de versão é criada (ex: `v1.0.1`)
5. ✅ Um GitHub Release é criado

### Processo de Deploy

A job `deploy-to-railway` executa os seguintes passos:

1. **Checkout do código**: Baixa o código-fonte do repositório
2. **Setup Railway CLI**: Instala a Railway CLI (versão mais recente)
3. **Deploy to Railway**: 
   - Navega para o diretório `back-end/`
   - Executa `railway up --service <SERVICE_ID> --detach`
   - O Railway faz o build usando o `nixpacks.toml`
   - A aplicação é deployada automaticamente

### Configuração do Backend

O backend está configurado para o Railway através dos seguintes arquivos:

#### `nixpacks.toml`
```toml
[phases.setup]
nixPkgs = ['openjdk17']

[phases.build]
cmds = ['./gradlew clean build -x test']

[start]
cmd = 'java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar build/libs/slotify-backend-0.0.1-SNAPSHOT.jar'
```

#### `application-prod.properties`
- Usa variáveis de ambiente do Railway:
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
- O Railway PostgreSQL exporta essas variáveis automaticamente

## Verificação do Deploy

Após o merge do PR, você pode acompanhar:

1. **GitHub Actions**: 
   - Vá em **Actions** no repositório
   - Verifique o workflow **Release**
   - Observe a execução da job **Deploy to Railway**

2. **Railway Dashboard**:
   - Acesse seu projeto no Railway
   - Veja os logs de deploy em tempo real
   - Verifique se o serviço está rodando corretamente

## Troubleshooting

### Erro: "railway: command not found"
- A action `railwayapp/setup-railway@v1` deve instalar a CLI automaticamente
- Verifique se a versão está configurada como `latest`

### Erro: "Authentication failed"
- Verifique se o `RAILWAY_TOKEN` está configurado corretamente
- Confirme que o token não expirou

### Erro: "Service not found"
- Verifique se o `RAILWAY_SERVICE_ID` está correto
- Confirme que o serviço existe no projeto Railway

### Deploy não acontece
- Verifique se o PR foi merged para `main` (não para `develop`)
- Confirme que o PR não era um draft
- Verifique se a job `create-release` foi bem-sucedida

## Segurança

⚠️ **IMPORTANTE**: 
- Nunca comite tokens ou secrets diretamente no código
- Use sempre GitHub Secrets para armazenar informações sensíveis
- Rotacione os tokens periodicamente
- Revogue tokens que não estão mais em uso

## Referências

- [Railway Documentation](https://docs.railway.app/)
- [Railway CLI](https://docs.railway.app/develop/cli)
- [GitHub Actions Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
