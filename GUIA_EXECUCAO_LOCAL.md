# Guia de Execução Local - Sistema Slotfy

Este guia fornece instruções completas para executar o sistema Slotfy localmente usando IntelliJ IDEA e acessar todas as funcionalidades pelo navegador.

## Pré-requisitos

### Software Necessário
- **Java 17** ou superior (JDK)
- **IntelliJ IDEA** (Community ou Ultimate)
- **Git** para clonagem do repositório
- **Navegador web** moderno (Chrome, Firefox, Safari, Edge)

### Verificação dos Pré-requisitos
```bash
# Verificar versão do Java
java -version

# Verificar versão do Git
git --version
```

## Configuração do Projeto

### 1. Clonagem do Repositório
```bash
git clone https://github.com/Filipe-Cutri/TCC_FHO.git
cd TCC_FHO
```

### 2. Importação no IntelliJ IDEA

1. Abra o IntelliJ IDEA
2. Selecione "Open" ou "Import Project"
3. Navegue até a pasta `TCC_FHO/back-end`
4. Selecione o arquivo `build.gradle`
5. Clique em "Open as Project"
6. Quando perguntado, selecione "Import Gradle Project"
7. Aguarde o download das dependências (pode levar alguns minutos)

### 3. Configuração do SDK

1. Vá em `File > Project Structure`
2. Em `Project Settings > Project`
3. Defina o Project SDK para Java 17
4. Clique em "Apply" e "OK"

## Execução da Aplicação

### Opção 1: Executar via IntelliJ IDEA

1. Localize a classe `SlotfyApplication.java` em `src/main/java/com/slotfy/`
2. Clique com o botão direito na classe
3. Selecione "Run 'SlotfyApplication'"
4. A aplicação iniciará e estará disponível em `http://localhost:8080`

### Opção 2: Executar via Terminal/Command Line

```bash
# Navegar para o diretório back-end
cd back-end

# Executar com perfil de teste (H2 database)
./gradlew bootRun -Dspring.profiles.active=test

# OU executar com perfil de produção (PostgreSQL - requer configuração)
./gradlew bootRun
```

### Configuração de Profiles

#### Profile de Teste (Recomendado para desenvolvimento local)
- **Ativação**: `-Dspring.profiles.active=test`
- **Banco de dados**: H2 (em memória)
- **Configuração**: `src/main/resources/application-test.properties`

#### Profile de Produção
- **Ativação**: Padrão ou `-Dspring.profiles.active=prod`
- **Banco de dados**: PostgreSQL
- **Configuração**: `src/main/resources/application-prod.properties`

## Acesso ao Sistema

### 1. Interface Principal
- **URL**: http://localhost:8080
- **Descrição**: Homepage do sistema com opções de acesso para clientes e estabelecimentos

### 2. Área do Cliente
- **Login**: http://localhost:8080/pages/client/client-login.html
- **Registro**: http://localhost:8080/pages/client/client-register.html
- **Dashboard**: http://localhost:8080/pages/client/client-dashboard.html

### 3. Área do Estabelecimento
- **Login**: http://localhost:8080/pages/establishment/establishment-login.html
- **Registro**: http://localhost:8080/pages/establishment/establishment-register.html
- **Dashboard**: http://localhost:8080/pages/establishment/establishment-dashboard.html

### 4. API REST
- **Base URL**: http://localhost:8080/api
- **Documentação**: http://localhost:8080/api/info
- **Health Check**: http://localhost:8080/api/health

## Acesso ao Banco de Dados H2

### Console H2 (Modo de Teste)
- **URL**: http://localhost:8080/h2-console
- **Driver Class**: `org.h2.Driver`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (deixar em branco)

### Tabelas Principais
- `clients` - Dados dos clientes
- `establishment_users` - Usuários dos estabelecimentos
- `establishments` - Dados dos estabelecimentos
- `professionals` - Profissionais cadastrados
- `services` - Serviços disponíveis
- `appointments` - Agendamentos realizados

## Testes e Validação

### Executar Testes Unitários
```bash
# Via Gradle
./gradlew test

# Via IntelliJ
# Clique com botão direito na pasta 'test' > Run 'All Tests'
```

### Teste Manual do Sistema

#### 1. Teste de Registro de Cliente
1. Acesse http://localhost:8080
2. Clique em "Sou Cliente"
3. Clique em "Não tem cadastro? Crie sua conta aqui"
4. Preencha o formulário com dados válidos
5. Verifique se o registro foi bem-sucedido

#### 2. Teste de Login de Cliente
1. Use as credenciais criadas no teste anterior
2. Acesse http://localhost:8080/pages/client/client-login.html
3. Faça login e verifique o redirecionamento para o dashboard

#### 3. Teste de API
```bash
# Teste de registro via API
curl -X POST http://localhost:8080/api/client/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Teste Cliente","email":"teste@exemplo.com","password":"123456","phone":"11999999999"}'

# Teste de login via API
curl -X POST http://localhost:8080/api/client/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@exemplo.com","password":"123456"}'
```

## Solução de Problemas

### Problema: Porta 8080 já está em uso
**Solução**: 
```bash
# Verificar processos na porta 8080
lsof -i :8080

# Matar processo se necessário
kill -9 <PID>

# OU alterar a porta no application.properties
server.port=8081
```

### Problema: Erro de compilação Java
**Solução**:
1. Verificar se o Java 17 está instalado
2. Configurar JAVA_HOME corretamente
3. Refresh do projeto Gradle no IntelliJ

### Problema: Dependências não baixadas
**Solução**:
```bash
# Limpar e rebuildar
./gradlew clean build

# No IntelliJ: File > Invalidate Caches and Restart
```

### Problema: H2 Console não conecta
**Solução**:
1. Verificar se o profile 'test' está ativo
2. Usar URL: `jdbc:h2:mem:testdb`
3. Username: `sa`, Password: (vazio)

## Configurações Avançadas

### Configuração do PostgreSQL (Produção)

1. Instalar PostgreSQL
2. Criar banco de dados:
```sql
CREATE DATABASE slotify;
CREATE USER slotify_user WITH PASSWORD 'slotify_password';
GRANT ALL PRIVILEGES ON DATABASE slotify TO slotify_user;
```

3. Atualizar `application-prod.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/slotify
spring.datasource.username=slotify_user
spring.datasource.password=slotify_password
```

### Logs e Debugging

#### Configuração de Logs
- **Localização**: Console do IntelliJ ou terminal
- **Nível**: Configurável em `application.properties`
```properties
logging.level.com.slotfy=DEBUG
logging.level.org.springframework.web=DEBUG
```

#### Debug no IntelliJ
1. Definir breakpoints no código
2. Executar em modo debug (ícone do inseto)
3. Usar as ferramentas de debug do IntelliJ

## Estrutura do Projeto

```
TCC_FHO/
├── back-end/                 # Aplicação Spring Boot
│   ├── src/main/java/        # Código fonte Java
│   ├── src/main/resources/   # Recursos e configurações
│   │   ├── static/           # Arquivos estáticos (frontend)
│   │   └── application*.properties
│   ├── src/test/java/        # Testes unitários
│   └── build.gradle          # Configuração do projeto
├── front-end/                # Arquivos frontend originais
├── docs/                     # Documentação
└── database_schema.sql       # Schema do banco de dados
```

## Recursos Implementados

### Funcionalidades do Cliente
- ✅ Registro de conta
- ✅ Login/Logout
- ✅ Dashboard personalizado
- ✅ Visualização de agendamentos
- ✅ Configuração de preferências

### Funcionalidades do Estabelecimento
- ✅ Registro de estabelecimento
- ✅ Login de usuários do estabelecimento
- ✅ Dashboard administrativo
- ✅ Gestão de profissionais
- ✅ Gestão de serviços

### API REST
- ✅ Autenticação de clientes
- ✅ Autenticação de estabelecimentos
- ✅ CRUD completo para todas as entidades
- ✅ Tratamento de erros global
- ✅ Validação de dados

### Banco de Dados
- ✅ Modelo relacional completo
- ✅ Migrations automáticas
- ✅ Suporte a H2 (desenvolvimento) e PostgreSQL (produção)
- ✅ Console H2 para inspeção de dados

## Contato e Suporte

Para dúvidas ou problemas:
1. Consulte este guia primeiro
2. Verifique os logs da aplicação
3. Consulte a documentação do Spring Boot
4. Entre em contato com a equipe de desenvolvimento

---

**Última atualização**: Agosto 2025  
**Versão do Sistema**: 1.0.0  
**Versão do Java**: 17  
**Versão do Spring Boot**: 3.2.0