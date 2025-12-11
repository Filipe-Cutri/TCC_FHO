# CI/CD - Integração e Deploy Contínuo

## Visão Geral

O projeto utiliza GitHub Actions para automação de testes, análise de código e deploy em produção.

## Pipelines Disponíveis

### 1. CI (Continuous Integration)
**Arquivo:** `.github/workflows/ci.yml`

**Triggers:**
- Push nas branches `main` e `develop`
- Pull requests para `main` e `develop`

**Etapas:**
1. Checkout do código
2. Setup JDK 17
3. Cache de dependências do SonarQube
4. Setup do Gradle
5. Execução dos testes com cobertura (JaCoCo)
6. Upload de cobertura para Codecov
7. Análise de código no SonarCloud

**Ferramentas:**
- **JaCoCo**: Cobertura de testes
- **Codecov**: Relatórios de cobertura
- **SonarCloud**: Análise estática de código (qualidade, bugs, vulnerabilidades)

### 2. Deploy (Continuous Deployment)
**Arquivo:** `.github/workflows/deploy.yml`

**Triggers:**
- Push na branch `main`
- Tags no formato `v*.*.*` (releases)
- Manualmente via `workflow_dispatch`

**Etapas:**
1. Checkout do código
2. Extração de informações de versão (tag, commit hash, data)
3. Configuração de variáveis de ambiente no Railway (backend e frontend)
4. Trigger de deploy no Railway

**Variáveis Configuradas:**
- `APP_VERSION`: Versão da aplicação (tag Git)
- `COMMIT_HASH`: Hash do commit
- `BUILD_DATE`: Data e hora do build
- `FRONTEND_URL`: URL do frontend em produção (para o backend)
- `BACKEND_URL`: URL do backend em produção (para o frontend)

### 3. Release
**Arquivo:** `.github/workflows/release.yml`

Workflow para criação de releases com changelog automático.

## Secrets Necessários

Configure os seguintes secrets no GitHub (Settings → Secrets and variables → Actions):

| Secret | Descrição | Obrigatório |
|--------|-----------|-------------|
| `RAILWAY_TOKEN` | Token de API do Railway | Sim |
| `SONAR_TOKEN` | Token do SonarCloud | Sim |
| `CODECOV_TOKEN` | Token do Codecov | Sim |
| `FRONTEND_URL` | URL do frontend em produção | Sim |
| `BACKEND_URL` | URL da API em produção | Sim |

### Como Obter os Tokens

**RAILWAY_TOKEN:**
1. Acesse [Railway Dashboard](https://railway.app)
2. Settings → Tokens → Create New Token

**SONAR_TOKEN:**
1. Acesse [SonarCloud](https://sonarcloud.io)
2. My Account → Security → Generate Token

**CODECOV_TOKEN:**
1. Acesse [Codecov](https://codecov.io)
2. Projeto → Settings → Copy token

## Monitoramento

### Status dos Pipelines
- CI: ![CI](https://github.com/Filipe-Cutri/TCC_FHO/actions/workflows/ci.yml/badge.svg)
- Quality Gate: ![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=Filipe-Cutri_TCC_FHO&metric=alert_status)
- Cobertura: ![Codecov](https://codecov.io/gh/Filipe-Cutri/TCC_FHO/branch/main/graph/badge.svg)

### Links Úteis
- [GitHub Actions](https://github.com/Filipe-Cutri/TCC_FHO/actions)
- [SonarCloud Dashboard](https://sonarcloud.io/summary/new_code?id=Filipe-Cutri_TCC_FHO)
- [Codecov Dashboard](https://codecov.io/gh/Filipe-Cutri/TCC_FHO)

## Fluxo de Trabalho

```
┌─────────────┐
│   Commit    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  CI Tests   │ ◄── Testes automáticos
└──────┬──────┘     Análise de código
       │            Cobertura
       ▼
┌─────────────┐
│  Code Review│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Merge     │
│   to main   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Deploy    │ ◄── Deploy automático
│   Railway   │     para produção
└─────────────┘
```

## Troubleshooting

### CI Falhando
- Verifique os logs no GitHub Actions
- Execute `./gradlew test` localmente
- Corrija os testes que falharam

### Deploy Falhando
- Verifique se `RAILWAY_TOKEN` está configurado
- Verifique se os serviços `backend` e `frontend` existem no Railway
- Verifique os logs do Railway

### SonarCloud com Erro
- Verifique se `SONAR_TOKEN` está válido
- Verifique a configuração em `build.gradle`
