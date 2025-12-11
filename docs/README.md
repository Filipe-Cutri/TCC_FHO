# Documentação do Slotfy

Documentação técnica completa do sistema de agendamento Slotfy.

## 📚 Índice

### 🏗️ [Arquitetura](./arquitetura/)
Arquitetura do sistema, estrutura de camadas e componentes.

**Tópicos:**
- Arquitetura de alto nível
- Backend (Spring Boot) - Controllers, Services, Repositories
- Frontend (Vanilla JS) - Estrutura de arquivos
- Integrações externas (AWS Bedrock, Gmail SMTP, S3)
- Fluxo de dados

### 🗄️ [Banco de Dados](./banco-de-dados/)
Schema do PostgreSQL, tabelas e relacionamentos.

**Tópicos:**
- Tabelas principais (establishments, clients, appointments, etc.)
- Relacionamentos e foreign keys
- Índices de performance
- Migrações com Flyway
- Validações e constraints

### 🚀 [Deployment](./deployment/)
Guias para executar o sistema localmente e fazer deploy em produção.

**Guias disponíveis:**
- [**localhost.md**](./deployment/localhost.md) - Como executar localmente
- [**railway.md**](./deployment/railway.md) - Como fazer deploy no Railway

### 🔧 [Configuração](./configuracao/)
Variáveis de ambiente, secrets e configurações.

**Tópicos:**
- Variáveis obrigatórias e opcionais
- GitHub Secrets
- Configuração no Railway
- Perfis do Spring Boot (dev, prod, test)
- Troubleshooting de configuração

### ⚙️ [CI/CD](./ci-cd/)
Pipelines de integração e deploy contínuo.

**Tópicos:**
- Workflow de CI (testes, análise de código)
- Workflow de Deploy (Railway)
- Secrets necessários
- Monitoramento e troubleshooting

## 🚀 Início Rápido

### Para Desenvolvedores

1. **Clone o repositório**
   ```bash
   git clone https://github.com/Filipe-Cutri/TCC_FHO.git
   ```

2. **Execute localmente**
   - Siga o guia: [Executar Localmente](./deployment/localhost.md)

3. **Entenda a arquitetura**
   - Leia: [Arquitetura do Sistema](./arquitetura/README.md)

### Para Deploy em Produção

1. **Configure o Railway**
   - Siga o guia: [Deploy no Railway](./deployment/railway.md)

2. **Configure Secrets no GitHub**
   - Siga: [Configuração de Secrets](./configuracao/README.md)

3. **Deploy automático**
   - Push na `main` ou crie uma tag de release

## 📖 Leitura Recomendada

### Primeira vez no projeto?
1. [README Principal](../README.md) - Visão geral do projeto
2. [Arquitetura](./arquitetura/README.md) - Entenda a estrutura
3. [Executar Localmente](./deployment/localhost.md) - Configure seu ambiente

### Vai fazer deploy?
1. [Configuração](./configuracao/README.md) - Configure variáveis
2. [Deploy no Railway](./deployment/railway.md) - Passo a passo completo

### Trabalhando com banco de dados?
1. [Banco de Dados](./banco-de-dados/README.md) - Schema e relacionamentos

### Configurando CI/CD?
1. [CI/CD](./ci-cd/README.md) - Pipelines e monitoramento

## 🔗 Links Úteis

### Produção
- **Frontend**: https://slotfy.com.br
- **Backend**: https://api.slotfy.com.br

### Monitoramento
- [GitHub Actions](https://github.com/Filipe-Cutri/TCC_FHO/actions)
- [SonarCloud](https://sonarcloud.io/summary/new_code?id=Filipe-Cutri_TCC_FHO)
- [Codecov](https://codecov.io/gh/Filipe-Cutri/TCC_FHO)

### Ferramentas
- [Railway Dashboard](https://railway.app)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)

## 🤝 Contribuindo

Este é um projeto acadêmico (TCC). Contribuições são bem-vindas!

1. Leia a documentação relevante
2. Faça suas alterações
3. Execute testes localmente
4. Abra um Pull Request

## 📝 Convenção de Documentação

- Arquivos em Markdown (`.md`)
- Linguagem: Português (BR)
- Estilo: Direto e objetivo
- Exemplos práticos sempre que possível
- Links internos para navegação fácil

---

**Dúvidas?** Abra uma issue no GitHub ou consulte a documentação específica de cada módulo.
