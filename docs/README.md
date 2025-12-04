# Documentação do Projeto Slotfy

Bem-vindo à documentação do Slotfy - Sistema de agendamento inteligente para barbearias e salões de beleza.

## 📚 Índice da Documentação

### 🚀 Início Rápido e Deploy

- **[Railway Configuration](./deployment/RAILWAY_CONFIGURATION.md)** - Guia completo de configuração no Railway
- **[Railway Deployment Fix](./deployment/RAILWAY_DEPLOYMENT_FIX.md)** - Soluções para problemas de deploy
- **[Localhost vs Railway](./deployment/LOCALHOST_VS_RAILWAY.md)** - Diferenças entre ambientes

### 🤖 Inteligência Artificial (Amazon Bedrock)

- **[Amazon Bedrock Setup Guide](./AMAZON_BEDROCK_SETUP.md)** - **NOVO!** Guia completo de configuração do Amazon Bedrock
- **[Bedrock Credentials Reference](./BEDROCK_CREDENTIALS_REFERENCE.md)** - **NOVO!** Referência rápida de credenciais

### 📧 Email e Notificações

- **[SendGrid Email Feature](./SENDGRID_EMAIL_FEATURE.md)** - Funcionalidade de email com SendGrid
- **[SendGrid Implementation Summary](./SENDGRID_IMPLEMENTATION_SUMMARY.md)** - Resumo da implementação

### 📸 Recursos e Funcionalidades

- **[Services & Professionals Image Upload](./SERVICES_PROFESSIONALS_IMAGE_UPLOAD.md)** - Upload de imagens
- **[Test Coverage Report](./TEST_COVERAGE_REPORT.md)** - Relatório de cobertura de testes
- **[Release Workflow](./RELEASE_WORKFLOW.md)** - Processo de release

## 🎯 Configuração Rápida

### Para Desenvolvedores (Primeira Vez)

1. **Clone o repositório**
   ```bash
   git clone https://github.com/Filipe-Cutri/TCC_FHO.git
   cd TCC_FHO
   ```

2. **Configure o Backend**
   ```bash
   cd back-end
   cp .env.template .env
   # Edite o arquivo .env com suas credenciais
   ```

3. **Configure as credenciais necessárias:**
   - SendGrid API Key (para emails)
   - Amazon Bedrock (para IA)
   - Database (PostgreSQL ou H2 para dev)

4. **Execute o backend**
   ```bash
   ./gradlew bootRun
   ```

### Para Deploy em Produção (Railway)

Siga os guias específicos:
1. [Railway Configuration](./deployment/RAILWAY_CONFIGURATION.md) - Setup inicial
2. [Amazon Bedrock Setup](./AMAZON_BEDROCK_SETUP.md) - Configurar IA
3. Configure as variáveis de ambiente no dashboard do Railway

## 🔑 Variáveis de Ambiente Essenciais

### Backend (Railway)

```env
# Database (fornecido automaticamente pelo Railway)
DATABASE_URL=postgres://...
SPRING_PROFILES_ACTIVE=prod

# Email
SENDGRID_API_KEY=SG.your-key
SENDGRID_FROM=noreply@slotfy.com

# Frontend
FRONTEND_URL=https://your-frontend.railway.app

# Amazon Bedrock (IA)
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
BEDROCK_MODEL_ID=meta.llama3-70b-instruct-v1:0
```

Ver `.env.template` no diretório `back-end/` para mais detalhes.

## 🏗️ Arquitetura do Projeto

```
TCC_FHO/
├── back-end/              # Spring Boot Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/slotfy/
│   │   │   │   ├── controller/    # REST Controllers
│   │   │   │   ├── service/       # Business Logic
│   │   │   │   │   ├── BedrockService.java     # IA Integration
│   │   │   │   │   ├── SuggestionService.java  # Scheduling AI
│   │   │   │   │   └── EmailService.java       # Email sending
│   │   │   │   ├── model/         # JPA Entities
│   │   │   │   └── repository/    # Data Access
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/  # Flyway migrations
│   │   └── test/          # Unit & Integration Tests
│   ├── .env.template      # Environment variables template
│   └── build.gradle       # Dependencies
│
├── front-end/             # Static Frontend
│   └── src/
│
└── docs/                  # Documentation (você está aqui!)
    ├── AMAZON_BEDROCK_SETUP.md           # Guia de IA
    ├── BEDROCK_CREDENTIALS_REFERENCE.md  # Credenciais
    └── deployment/                       # Deploy guides
```

## 🔧 Tecnologias Utilizadas

### Backend
- **Java 17** - Linguagem principal
- **Spring Boot 3.2** - Framework
- **PostgreSQL** - Banco de dados (produção)
- **H2** - Banco de dados (desenvolvimento)
- **AWS SDK** - Integração com Amazon Bedrock
- **SendGrid** - Envio de emails
- **Flyway** - Migrations de banco
- **JUnit 5** - Testes

### Frontend
- **HTML5/CSS3** - Interface
- **JavaScript** - Lógica client-side
- **Bootstrap** - Framework CSS

### DevOps
- **Railway** - Hospedagem e deploy
- **GitHub Actions** - CI/CD
- **Gradle** - Build tool
- **Docker** (via Nixpacks) - Containerização

## 📊 Features Principais

### ✅ Implementadas

1. **Sistema de Autenticação**
   - Login de clientes e estabelecimentos
   - Recuperação de senha via email
   - Sessões seguras

2. **Gerenciamento de Agendamentos**
   - Criar/editar/cancelar agendamentos
   - Visualização de calendário
   - Notificações por email

3. **Profissionais e Serviços**
   - Cadastro de profissionais
   - Upload de imagens
   - Gerenciamento de serviços

4. **Inteligência Artificial (Amazon Bedrock)**
   - Sugestões inteligentes de horários
   - Análise de disponibilidade
   - Otimização de agenda

5. **Notificações por Email**
   - Confirmação de agendamento
   - Lembretes
   - Reset de senha

### 🚧 Em Desenvolvimento

- Dashboard de relatórios
- Sistema de avaliações
- Integração com calendário Google
- App mobile

## 🧪 Testes

### Executar todos os testes

```bash
cd back-end
./gradlew test
```

### Executar testes específicos

```bash
./gradlew test --tests "BedrockServiceTest"
./gradlew test --tests "EmailServiceTest"
```

### Ver relatório de cobertura

```bash
./gradlew test jacocoTestReport
# Abra: build/reports/jacoco/test/html/index.html
```

## 📝 Contribuindo

1. Crie uma branch para sua feature
2. Implemente e teste suas mudanças
3. Atualize a documentação relevante
4. Abra um Pull Request

## 🔐 Segurança

### ⚠️ Boas Práticas

- **NUNCA** commite credenciais no código
- Use variáveis de ambiente para todos os secrets
- Rotacione credenciais regularmente
- Configure limites de gastos na AWS
- Mantenha dependências atualizadas

### 🔒 Credenciais Sensíveis

As seguintes informações são sensíveis e devem ser protegidas:
- AWS Access Keys (Bedrock)
- SendGrid API Key
- Database passwords
- JWT secrets (se implementado)

## 📞 Suporte

### Problemas com Deploy?
- Veja [Railway Deployment Fix](./deployment/RAILWAY_DEPLOYMENT_FIX.md)
- Verifique logs no Railway dashboard

### Problemas com Amazon Bedrock?
- Veja [Amazon Bedrock Setup](./AMAZON_BEDROCK_SETUP.md)
- Verifique credenciais em [Bedrock Reference](./BEDROCK_CREDENTIALS_REFERENCE.md)

### Problemas com Emails?
- Veja [SendGrid Implementation](./SENDGRID_IMPLEMENTATION_SUMMARY.md)
- Verifique configuração do SendGrid

## 📚 Recursos Externos

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [SendGrid Documentation](https://docs.sendgrid.com/)
- [Railway Documentation](https://docs.railway.app/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 📄 Licença

Este projeto é um TCC (Trabalho de Conclusão de Curso) desenvolvido para a FHO.

---

**Última atualização**: Dezembro 2024
**Versão**: 1.0.0

Para mais informações, consulte o [README principal](../README.md) do projeto.
