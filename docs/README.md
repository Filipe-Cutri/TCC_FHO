# Documentação Slotfy

Bem-vindo à documentação completa do sistema Slotfy - plataforma de gerenciamento para estabelecimentos de beleza.

## 📚 Índice Geral

### 🚀 Setup e Configuração
- [Guia de Execução Local](setup/GUIA_EXECUCAO_LOCAL.md) - Como executar o projeto em ambiente de desenvolvimento
- [Configuração IntelliJ](setup/INTELLIJ_SETUP.md) - Setup da IDE
- [Configuração SSL/HTTPS](setup/SSL_HTTPS_SETUP.md) - Configuração de segurança
- [Setup JaCoCo](setup/JACOCO_SETUP.md) - Configuração de cobertura de testes

### 🏗️ Arquitetura
- [Arquitetura Geral](architecture/ARCHITECTURE.md) - Visão geral da arquitetura do sistema
- [Estrutura Spring Boot](architecture/SPRING_BOOT_STRUCTURE.md) - Organização do backend
- [Integração Frontend-Backend](architecture/FRONTEND_BACKEND_INTEGRATION.md) - Como frontend e backend se comunicam
- [Arquitetura Visual](architecture/VISUAL_ARCHITECTURE.md) - Diagramas visuais
- [Diagramas UML](architecture/DIAGRAMAS_UML.md) - Diagramas de classes e sequência
- [Diagrama ER](architecture/DIAGRAMA_ER.md) - Diagrama de Entidade-Relacionamento

### 🗄️ Banco de Dados
- [Tabelas do Banco](database/TABELAS_BANCO_DADOS.md) - Descrição completa das tabelas
- [Resumo das Tabelas](database/RESUMO_TABELAS.md) - Visão resumida

### 💻 Desenvolvimento

#### Funcionalidades e Integrações
- **[📋 Funcionalidades Completas e Integrações](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md)** ⭐ **NOVO**
  - Detalhamento completo de todas as funcionalidades por módulo/tela
  - Especificação de operações CRUD para cada entidade
  - Documentação de todos os endpoints da API
  - Fluxos de interação com cliente final
  - Exemplos de requisições e respostas

- **[🔔 Sistema de Notificações](development/SISTEMA_NOTIFICACOES.md)** ⭐ **NOVO**
  - Tipos de notificações automáticas
  - Lembretes de agendamento
  - Confirmações online
  - Integrações de email, SMS e push
  - Templates de comunicação
  - Preferências do cliente

- **[🗺️ Roadmap de Implementação](development/ROADMAP_IMPLEMENTACAO.md)** ⭐ **NOVO**
  - Fases de desenvolvimento
  - Priorização de tarefas
  - Cronograma estimado
  - Recursos necessários
  - Métricas de sucesso

#### Outros Documentos de Desenvolvimento
- [Solução Técnica](development/SOLUTION.md) - Decisões técnicas e arquiteturais
- [CI/CD Setup](development/CI_CD_SETUP.md) - Configuração de integração e deploy contínuo
- [Verificação de Autenticação](development/AUTHENTICATION_VERIFICATION_REPORT.md) - Relatório de autenticação

### 📊 Qualidade e Testes
- [Relatório de Cobertura](../back-end/RELATORIO_COBERTURA_TESTES.md) - Cobertura de testes do backend
- [Relatório de Testes](TEST_COVERAGE_REPORT.md) - Relatório consolidado de testes

---

## 🎯 Documentos Principais por Perfil

### Para Desenvolvedores Backend
1. [Estrutura Spring Boot](architecture/SPRING_BOOT_STRUCTURE.md)
2. **[Funcionalidades Completas e Integrações](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md)** - Especificação de APIs
3. [Tabelas do Banco](database/TABELAS_BANCO_DADOS.md)
4. **[Sistema de Notificações](development/SISTEMA_NOTIFICACOES.md)** - Implementação de notificações
5. [Guia de Execução Local](setup/GUIA_EXECUCAO_LOCAL.md)

### Para Desenvolvedores Frontend
1. [Integração Frontend-Backend](architecture/FRONTEND_BACKEND_INTEGRATION.md)
2. **[Funcionalidades Completas e Integrações](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md)** - Fluxos de interação
3. **[Sistema de Notificações](development/SISTEMA_NOTIFICACOES.md)** - Interface de notificações
4. [Guia de Execução Local](setup/GUIA_EXECUCAO_LOCAL.md)

### Para Product Owners / Gerentes de Projeto
1. **[Funcionalidades Completas e Integrações](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md)** - Especificação completa
2. **[Roadmap de Implementação](development/ROADMAP_IMPLEMENTACAO.md)** - Planejamento e cronograma
3. [Arquitetura Geral](architecture/ARCHITECTURE.md)
4. [Relatório de Testes](TEST_COVERAGE_REPORT.md)

### Para DevOps / SRE
1. [CI/CD Setup](development/CI_CD_SETUP.md)
2. [Configuração SSL/HTTPS](setup/SSL_HTTPS_SETUP.md)
3. [Guia de Execução Local](setup/GUIA_EXECUCAO_LOCAL.md)
4. **[Sistema de Notificações](development/SISTEMA_NOTIFICACOES.md)** - Configurações de infraestrutura

---

## 📖 Guias Rápidos

### Começando no Projeto
1. Leia o [README principal](../README.md)
2. Configure o ambiente com o [Guia de Execução Local](setup/GUIA_EXECUCAO_LOCAL.md)
3. Entenda a [Arquitetura Geral](architecture/ARCHITECTURE.md)
4. Explore as [Funcionalidades Completas](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md)

### Implementando uma Nova Feature
1. Consulte o [Roadmap de Implementação](development/ROADMAP_IMPLEMENTACAO.md)
2. Revise as [Funcionalidades Completas](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md) para o módulo
3. Verifique a [Estrutura Spring Boot](architecture/SPRING_BOOT_STRUCTURE.md)
4. Consulte as [Tabelas do Banco](database/TABELAS_BANCO_DADOS.md) se necessário
5. Siga as [Integrações Frontend-Backend](architecture/FRONTEND_BACKEND_INTEGRATION.md)

### Adicionando Notificações
1. Leia o [Sistema de Notificações](development/SISTEMA_NOTIFICACOES.md)
2. Implemente o backend conforme especificado
3. Crie templates de email
4. Configure SMTP
5. Teste com usuários reais

---

## 🔍 Busca Rápida

### Por Funcionalidade

**Clientes:**
- Registro e Login: [Funcionalidades Completas - Seção 1.1 e 1.2](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#11-tela-cliente---registro-client-registerhtml)
- Dashboard: [Funcionalidades Completas - Seção 1.3](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#13-tela-cliente---dashboard-client-dashboardhtml)
- Perfil: [Funcionalidades Completas - Seção 1.4](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#14-tela-cliente---perfil-client-profilehtml)
- Agendamentos: [Funcionalidades Completas - Seção 1.8](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#18-tela-cliente---agendamentos-client-bookingshtml)

**Estabelecimentos:**
- Dashboard: [Funcionalidades Completas - Seção 2.1](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#21-tela-estabelecimento---dashboard-establishment-dashboardhtml)
- Gestão de Agendamentos: [Funcionalidades Completas - Seção 2.2](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#22-tela-estabelecimento---agendamentos-establishment-appointmentshtml)
- Gestão de Serviços: [Funcionalidades Completas - Seção 2.3](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#23-tela-estabelecimento---serviços-establishment-serviceshtml)
- Gestão de Profissionais: [Funcionalidades Completas - Seção 2.4](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#24-tela-estabelecimento---profissionais-establishment-professionalshtml)

**Notificações:**
- Visão Geral: [Sistema de Notificações - Seção 1](development/SISTEMA_NOTIFICACOES.md#1-tipos-de-notificações)
- Email: [Sistema de Notificações - Seção 5](development/SISTEMA_NOTIFICACOES.md#5-implementação-de-email)
- APIs: [Sistema de Notificações - Seção 3](development/SISTEMA_NOTIFICACOES.md#3-apis-de-notificação)

### Por Endpoint

Todos os endpoints estão documentados em:
- [Funcionalidades Completas - Resumo de Endpoints](development/FUNCIONALIDADES_COMPLETAS_INTEGRACOES.md#resumo-de-endpoints-por-módulo)

### Por Tabela

Todas as tabelas estão documentadas em:
- [Tabelas do Banco de Dados](database/TABELAS_BANCO_DADOS.md)

---

## 🆕 Últimas Atualizações

### Outubro 2025
- ✨ **NOVO:** Documentação completa de funcionalidades e integrações
- ✨ **NOVO:** Especificação detalhada do sistema de notificações
- ✨ **NOVO:** Roadmap de implementação com cronograma
- 📝 Atualização da documentação de arquitetura
- 📝 Melhorias nos guias de setup

---

## 🤝 Contribuindo

Para contribuir com a documentação:

1. Mantenha o formato Markdown consistente
2. Adicione exemplos práticos quando possível
3. Use diagramas para conceitos complexos
4. Mantenha os links internos atualizados
5. Documente decisões de design importantes

---

## 📞 Suporte

Para dúvidas sobre a documentação:
- Abra uma issue no GitHub
- Entre em contato com a equipe de desenvolvimento
- Consulte o [README principal](../README.md) para informações de contato

---

*Última atualização: Outubro 2025*  
*Mantenedor: Equipe Slotfy*
