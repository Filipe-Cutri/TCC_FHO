# Release Workflow Documentation

## Visão Geral

O workflow de Release (`release.yml`) foi criado para automatizar o processo de versionamento e criação de releases do projeto TCC_FHO - Slotfy. Ele trabalha em conjunto com o workflow de CI existente (`ci.yml`) para fornecer um pipeline completo de validação e deployment.

## Características Principais

### 1. Triggers
O workflow é acionado nos seguintes eventos de Pull Request:
- `opened`: Quando um PR é aberto
- `reopened`: Quando um PR é reaberto
- `synchronize`: Quando novos commits são adicionados ao PR
- `closed`: Quando um PR é fechado (merged ou não)

### 2. Jobs Condicionais

#### **draft-pr-check**
- **Quando executa**: Apenas quando `PR.draft == true`
- **O que faz**:
  - Validações básicas da estrutura do projeto
  - Verifica a existência dos diretórios principais (back-end, front-end)
  - Feedback rápido para PRs em desenvolvimento

#### **ready-pr-check**
- **Quando executa**: Quando `PR.draft == false` e PR não foi merged
- **O que faz**:
  - Validações completas do projeto
  - Build do backend com Gradle
  - Execução de testes
  - Garante que PRs prontos para revisão estão funcionais

#### **create-release**
- **Quando executa**: Quando PR é merged para `main` e `PR.draft == false`
- **O que faz**:
  1. Busca a última tag semântica existente (formato `v*.*.*`)
  2. Incrementa automaticamente a versão PATCH (`v1.0.0` → `v1.0.1`)
  3. Cria e faz push da nova tag
  4. Gera notas de release automáticas
  5. Cria uma GitHub Release pública

### 3. Versionamento Semântico Automático

O workflow implementa versionamento semântico seguindo o padrão:
- **MAJOR.MINOR.PATCH** (ex: `v1.2.3`)
- Incremento automático do **PATCH** a cada merge

**Lógica de versionamento:**
```bash
# Se não existir tag anterior
VERSÃO_INICIAL = v0.0.0

# A cada merge para main
NOVA_VERSÃO = v{MAJOR}.{MINOR}.{PATCH + 1}
```

### 4. Notas de Release

As notas de release são geradas automaticamente e incluem:
- Número e título do PR merged
- Autor do PR
- Lista de commits desde a última release
- Link para o changelog completo

### 5. Segurança

- Usa `GITHUB_TOKEN` (fornecido automaticamente pelo GitHub Actions)
- Permissões mínimas necessárias:
  - `contents: write` - Para criar tags e releases
  - `pull-requests: read` - Para ler informações do PR
- Não requer configuração de secrets adicionais

## Integração com CI Workflow

O workflow `ci.yml` foi atualizado para complementar o `release.yml`:

```yaml
if: github.event_name == 'push' || github.event.pull_request.draft == false
```

**Comportamento:**
- **Push para branch**: Sempre executa testes e análise SonarQube
- **PR Draft**: Não executa (economiza recursos)
- **PR Ready**: Executa testes completos e análise SonarQube

## Fluxo de Trabalho Completo

### Para PRs em Draft:
1. Desenvolvedor abre PR em modo draft
2. `release.yml` → job `draft-pr-check` executa validações básicas
3. `ci.yml` → não executa (economiza recursos)

### Para PRs Ready for Review:
1. Desenvolvedor marca PR como "Ready for review"
2. `release.yml` → job `ready-pr-check` executa build e testes
3. `ci.yml` → job `test-and-sonar` executa testes com coverage e SonarQube

### Para PR Merged:
1. PR é aprovado e merged para `main`
2. `release.yml` → job `create-release` executa:
   - Calcula nova versão (incrementa PATCH)
   - Cria tag `v*.*.*`
   - Gera release notes
   - Publica GitHub Release

## Exemplos de Uso

### Exemplo 1: Primeiro Release
```
Estado inicial: Sem tags
Merge do PR #1 → Cria tag v0.0.1 e Release
```

### Exemplo 2: Release Subsequente
```
Estado atual: v0.0.1
Merge do PR #2 → Cria tag v0.0.2 e Release
Merge do PR #3 → Cria tag v0.0.3 e Release
```

### Exemplo 3: Workflow Draft → Ready
```
1. Abrir PR em draft → draft-pr-check executa
2. Adicionar commits → draft-pr-check executa novamente
3. Marcar como "Ready" → ready-pr-check executa
4. Aprovar e merge → create-release executa
```

## Requisitos

- GitHub Actions habilitado no repositório
- Branch `main` como branch principal
- Permissões padrão de GITHUB_TOKEN
- Gradle configurado no projeto (para ready-pr-check)

## Troubleshooting

### Tag não foi criada
- Verificar se o PR foi merged para `main`
- Verificar se o PR não estava em modo draft
- Conferir logs do job `create-release`

### Release não apareceu
- Verificar permissões do GITHUB_TOKEN
- Conferir se a tag foi criada corretamente
- Verificar logs do step "Create GitHub Release"

### Build falhou no ready-pr-check
- Verificar se o código compila localmente
- Conferir se todas as dependências estão disponíveis
- Revisar logs do Gradle

## Próximos Passos (Melhorias Futuras)

1. **Versionamento Inteligente**: Detectar breaking changes para incrementar MAJOR ou MINOR
2. **Changelog Automático**: Gerar CHANGELOG.md baseado em commits convencionais
3. **Artifacts**: Anexar binários compilados à release
4. **Notificações**: Integrar com Slack/Discord para notificar novas releases
5. **Rollback**: Adicionar workflow para reverter releases problemáticas

## Referências

- [GitHub Actions - Pull Request Events](https://docs.github.com/en/actions/using-workflows/events-that-trigger-workflows#pull_request)
- [Semantic Versioning](https://semver.org/)
- [softprops/action-gh-release](https://github.com/softprops/action-gh-release)
