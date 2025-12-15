# Slotfy

[![CI](https://github.com/Filipe-Cutri/TCC_FHO/actions/workflows/ci.yml/badge.svg)](https://github.com/Filipe-Cutri/TCC_FHO/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Filipe-Cutri_TCC_FHO&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Filipe-Cutri_TCC_FHO)
[![codecov](https://codecov.io/gh/Filipe-Cutri/TCC_FHO/branch/main/graph/badge.svg)](https://codecov.io/gh/Filipe-Cutri/TCC_FHO)

Sistema de agendamento online para barbearias, salões de beleza e profissionais da área de estética. Conecta clientes aos melhores profissionais da região de forma prática e inteligente.

## 👥 Autores

**- Aluno:** Filipe Alberto Cutri 

**- Curso:** Sistemas de Informação

**- Universidade:** Fundação Hermínio Ometto 

## 📋 Sobre o Projeto

O Slotfy é uma plataforma web desenvolvida como Trabalho de Conclusão de Curso (TCC) da FHO. O sistema permite que estabelecimentos gerenciem seus serviços, profissionais e agendamentos, enquanto clientes podem encontrar e agendar serviços de forma simples e rápida.

**Principais funcionalidades:**
- Cadastro e gestão de estabelecimentos
- Cadastro e gestão de serviços e profissionais
- Sistema de agendamento online
- Painel administrativo para estabelecimentos
- Área do cliente para gerenciar agendamentos
- Recuperação de senha via e-mail

## 🛠️ Tecnologias

**Backend:**
- Java 17
- Spring Boot 3.2.0
- PostgreSQL
- Spring Security
- Spring Data JPA
- Flyway (migrações de banco de dados)
- AWS Bedrock (IA)

**Frontend:**
- HTML5, CSS3, JavaScript (Vanilla)
- Font Awesome
- Design responsivo

**CI/CD:**
- GitHub Actions
- Railway (deploy em produção)
- SonarCloud (análise de código)
- Codecov (cobertura de testes)

## 📁 Estrutura do Projeto

```
TCC_FHO/
├── back-end/              # Backend (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/     # Código Java
│   │   │   └── resources/ # Configurações e migrations
│   │   └── test/         # Testes unitários
│   ├── build.gradle      # Dependências e build
│   └── nixpacks.toml     # Configuração Railway
│
├── front-end/             # Frontend (HTML/CSS/JS)
│   ├── src/
│   │   ├── assets/       # Imagens, ícones, CSS, JS
│   │   └── pages/        # Páginas HTML
│   ├── inject-config.sh  # Script de configuração
│   └── nixpacks.toml     # Configuração Railway
│
├── database/              # Scripts de banco de dados
│   ├── database_schema.sql
│   └── database_migration_*.sql
│
├── docs/                  # Documentação técnica
│   ├── arquitetura/
│   ├── banco-de-dados/
│   ├── ci-cd/
│   ├── configuracao/
│   └── deployment/
│
├── .github/
│   └── workflows/        # Pipelines CI/CD
│
└── scripts/              # Scripts auxiliares
```

## 🚀 Documentação

Acesse a documentação completa em [`docs/`](./docs/):

- **[Deployment](./docs/deployment/)** - Como fazer deploy no Railway e executar localmente
- **[Arquitetura](./docs/arquitetura/)** - Arquitetura do sistema e diagramas
- **[Banco de Dados](./docs/banco-de-dados/)** - Schema, migrações e relacionamentos
- **[CI/CD](./docs/ci-cd/)** - Pipelines de integração e deploy contínuo
- **[Configuração](./docs/configuracao/)** - Variáveis de ambiente e secrets

## 💻 Como Executar

### Desenvolvimento Local
Consulte o guia completo em [docs/deployment/localhost.md](./docs/deployment/localhost.md)

### Deploy em Produção (Railway)
Consulte o guia completo em [docs/deployment/railway.md](./docs/deployment/railway.md)

## 📝 Convenção de Commits

Este projeto segue a convenção de commits semânticos:

```
tipo(escopo): descrição curta

Descrição detalhada (opcional)
```

**Tipos:**
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Documentação
- `style`: Formatação, ponto e vírgula, etc
- `refactor`: Refatoração de código
- `test`: Adição ou modificação de testes
- `chore`: Atualizações de build, dependências, etc

**Exemplos:**
```
feat(agendamento): adiciona validação de horários disponíveis
fix(auth): corrige erro no login de clientes
docs(readme): atualiza instruções de instalação
```

## 📄 Licença

Este projeto foi desenvolvido como Trabalho de Conclusão de Curso da FHO.
