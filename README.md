# TCC_FHO - Slotfy

[![CI](https://github.com/Filipe-Cutri/TCC_FHO/actions/workflows/ci.yml/badge.svg)](https://github.com/Filipe-Cutri/TCC_FHO/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Filipe-Cutri_TCC_FHO&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Filipe-Cutri_TCC_FHO)
[![codecov](https://codecov.io/gh/Filipe-Cutri/TCC_FHO/branch/main/graph/badge.svg)](https://codecov.io/gh/Filipe-Cutri/TCC_FHO)


Sistema de agendamento inteligente para barbearias e salões de beleza.

## 📖 Documentação

Toda a documentação do projeto foi organizada na pasta `docs/`. Acesse [docs/README.md](docs/README.md) para navegar pela documentação completa.

## 🚀 Início Rápido

### Desenvolvimento Local
Para executar o projeto localmente, consulte o [Guia de Execução Local](docs/setup/GUIA_EXECUCAO_LOCAL.md).

### Deploy em Produção (Railway)
Para configurar e fazer deploy no Railway, consulte:
- [Guia de Configuração do Railway](docs/deployment/RAILWAY_CONFIGURATION.md)
- [Diferenças entre Localhost e Railway](docs/deployment/LOCALHOST_VS_RAILWAY.md)

### Configuração do Amazon Bedrock (IA)
Para configurar a integração com Amazon Bedrock para funcionalidades de IA:
- [Guia Completo de Setup do Amazon Bedrock](docs/AMAZON_BEDROCK_SETUP.md)
- [Referência Rápida de Credenciais](docs/BEDROCK_CREDENTIALS_REFERENCE.md)
- Scripts de configuração: `back-end/setup_bedrock.sh` (Linux/Mac) ou `back-end/setup_bedrock.ps1` (Windows)

## 🏗️ Estrutura do Projeto

```
TCC_FHO/
├── docs/                     # 📚 Documentação organizada
│   ├── setup/               # Guias de configuração
│   ├── deployment/          # Guias de deploy (Railway, etc)
│   ├── architecture/        # Diagramas e arquitetura
│   ├── database/           # Documentação do banco
│   ├── requirements/       # Requisitos e tecnologias
│   └── development/        # Docs de desenvolvimento
├── front-end/              # 🎨 Frontend (HTML, CSS, JS)
├── back-end/               # ⚙️ Backend (Spring Boot)
├── database_schema.sql     # 🗄️ Schema do banco
└── assets/                 # 📁 Assets do projeto
