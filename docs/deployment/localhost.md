# Executar Localmente (Localhost)

## Pré-requisitos

### Ferramentas Necessárias

1. **Java JDK 17**
   - Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) ou [OpenJDK](https://adoptium.net/)
   - Verifique: `java -version`

2. **PostgreSQL**
   - Download: [PostgreSQL](https://www.postgresql.org/download/)
   - Versão recomendada: 14 ou superior
   - Verifique: `psql --version`

3. **Git**
   - Download: [Git](https://git-scm.com/downloads)
   - Verifique: `git --version`

4. **Navegador Web Moderno**
   - Chrome, Firefox, Edge ou Safari

### Opcional (mas recomendado)

5. **IDE**
   - [IntelliJ IDEA](https://www.jetbrains.com/idea/) (Community ou Ultimate)
   - [VS Code](https://code.visualstudio.com/) com extensões Java

6. **Node.js** (para servir o frontend)
   - Download: [Node.js](https://nodejs.org/)
   - Apenas se quiser usar `serve` ou `http-server`

## Configuração do Banco de Dados

### 1. Instalar PostgreSQL

Siga as instruções de instalação para seu sistema operacional.

### 2. Criar Banco de Dados

```bash
# Acessar PostgreSQL
psql -U postgres

# Criar banco de dados
CREATE DATABASE slotfy_db;

# Criar usuário (opcional)
CREATE USER slotfy_user WITH PASSWORD 'slotfy_password';

# Dar permissões
GRANT ALL PRIVILEGES ON DATABASE slotfy_db TO slotfy_user;

# Sair
\q
```

### 3. Importar Schema (Opcional)

Se quiser importar o schema manualmente:

```bash
psql -U postgres -d slotfy_db -f database/database_schema.sql
```

> **Nota:** O Flyway criará as tabelas automaticamente na primeira execução do backend.

## Configuração do Backend

### 1. Clonar Repositório

```bash
git clone https://github.com/Filipe-Cutri/TCC_FHO.git
cd TCC_FHO
```

### 2. Configurar application.properties

O arquivo `back-end/src/main/resources/application.properties` já está configurado para desenvolvimento local.

Verifique se as configurações do banco estão corretas:

```properties
# application.properties já tem configuração para dev
spring.profiles.active=dev
```

Crie um arquivo `application-dev.properties` se necessário:

```properties
# Banco de dados local
spring.datasource.url=jdbc:postgresql://localhost:5432/slotfy_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
```

### 3. Compilar e Executar

#### Via Gradle (Linha de Comando)

```bash
cd back-end

# Linux/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

#### Via IntelliJ IDEA

1. Abra o projeto `back-end` no IntelliJ
2. Espere o Gradle sincronizar
3. Encontre a classe `SlotfyApplication.java`
4. Clique com botão direito → Run 'SlotfyApplication'

#### Via VS Code

1. Abra a pasta `back-end` no VS Code
2. Instale a extensão "Extension Pack for Java"
3. Pressione F5 para debug ou Ctrl+F5 para run

### 4. Verificar Backend

O backend deve estar rodando em:
```
https://localhost:8443
```

Teste no navegador ou com curl:
```bash
curl https://localhost:8443/
# Deve retornar JSON com info da API
```

> **Nota:** Como o backend usa HTTPS com certificado self-signed, o navegador mostrará um aviso de segurança. Clique em "Avançado" → "Prosseguir para localhost".

## Configuração do Frontend

### 1. Configurar Backend URL

Edite o arquivo `front-end/src/config.js` e configure a URL do backend:

```javascript
window.BACKEND_URL = 'https://localhost:8443';
```

Se o arquivo não existir, crie-o:

```bash
cd front-end/src
cat > config.js << 'EOF'
window.BACKEND_URL = 'https://localhost:8443';

window.APP_CONFIG = {
    environment: 'development',
    apiVersion: 'v1'
};
EOF
```

### 2. Servir o Frontend

Você tem várias opções:

#### Opção 1: Python (mais simples)

```bash
cd front-end/src

# Python 3
python -m http.server 3000

# Acesse: http://localhost:3000
```

#### Opção 2: Node.js (http-server)

```bash
# Instalar http-server globalmente
npm install -g http-server

cd front-end/src
http-server -p 3000

# Acesse: http://localhost:3000
```

#### Opção 3: Node.js (serve)

```bash
# Instalar serve globalmente
npm install -g serve

cd front-end/src
serve -l 3000

# Acesse: http://localhost:3000
```

#### Opção 4: VS Code Live Server

1. Instale a extensão "Live Server" no VS Code
2. Abra `front-end/src/index.html`
3. Clique com botão direito → "Open with Live Server"

### 3. Acessar a Aplicação

Abra o navegador em:
```
http://localhost:3000
```

## Estrutura de Execução

```
┌─────────────────────────┐
│   Navegador             │
│   http://localhost:3000 │
└───────────┬─────────────┘
            │
            │ REST API
            ▼
┌─────────────────────────┐
│   Backend (Spring Boot) │
│   https://localhost:8443│
└───────────┬─────────────┘
            │
            │ JDBC
            ▼
┌─────────────────────────┐
│   PostgreSQL            │
│   localhost:5432        │
└─────────────────────────┘
```

## Variáveis de Ambiente Locais

### Backend

Você pode configurar via IDE ou linha de comando:

```bash
# Exemplo via linha de comando
export SPRING_PROFILES_ACTIVE=dev
export DATABASE_URL=jdbc:postgresql://localhost:5432/slotfy_db

./gradlew bootRun
```

### Frontend

Edite `front-end/src/config.js` diretamente (não usa variáveis de ambiente em desenvolvimento local).

## Testes

### Executar Testes do Backend

```bash
cd back-end

# Executar todos os testes
./gradlew test

# Executar com relatório de cobertura
./gradlew test jacocoTestReport

# Ver relatório
open build/reports/jacoco/test/html/index.html
```

## Desenvolvimento

### Hot Reload

#### Backend (Spring Boot DevTools)

Já está configurado. Mudanças em código Java são recarregadas automaticamente.

#### Frontend

Use Live Server (VS Code) ou `serve` com watch mode para recarregar automaticamente.

### Debug

#### Backend no IntelliJ

1. Adicione breakpoints no código
2. Run → Debug 'SlotfyApplication'

#### Backend no VS Code

1. Adicione breakpoints
2. Pressione F5

#### Frontend

Use DevTools do navegador (F12):
- Console para logs
- Network para requisições
- Debugger para breakpoints em JavaScript

## Troubleshooting

### PostgreSQL não conecta

**Erro:**
```
org.postgresql.util.PSQLException: Connection refused
```

**Solução:**
- Verifique se PostgreSQL está rodando: `pg_isready`
- Inicie PostgreSQL:
  - Linux: `sudo systemctl start postgresql`
  - Mac: `brew services start postgresql`
  - Windows: Serviços → PostgreSQL → Iniciar

### Porta 8443 já em uso

**Erro:**
```
Port 8443 already in use
```

**Solução:**
```bash
# Encontrar processo usando a porta
lsof -i :8443  # Mac/Linux
netstat -ano | findstr :8443  # Windows

# Matar processo
kill -9 <PID>
```

Ou configure outra porta em `application.properties`:
```properties
server.port=8444
```

### Frontend não consegue chamar API

**Sintomas:**
- Erros CORS no console
- Requisições falhando

**Solução:**
1. Verifique se `config.js` tem a URL correta
2. Verifique se `config.js` é carregado ANTES de `api-config.js`
3. Verifique se backend está rodando
4. Aceite o certificado self-signed acessando `https://localhost:8443` diretamente

### Flyway migration error

**Erro:**
```
FlywayException: Validate failed
```

**Solução:**
```bash
# Limpar banco e reiniciar
psql -U postgres -d slotfy_db -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"

# Reiniciar backend (Flyway recriará as tabelas)
```

### Gradle build falha

**Erro:**
```
Could not resolve dependencies
```

**Solução:**
```bash
cd back-end

# Limpar cache do Gradle
./gradlew clean

# Rebuild
./gradlew build --refresh-dependencies
```

## Credenciais de Teste

Após iniciar a aplicação, você pode criar usuários através das páginas de registro.

### Cliente de Teste

1. Acesse `http://localhost:3000/pages/client/client-register.html`
2. Preencha o formulário
3. Faça login

### Estabelecimento de Teste

1. Acesse a página de registro de estabelecimento
2. Crie um estabelecimento
3. Faça login

## Próximos Passos

- Consulte [Arquitetura](../arquitetura/README.md) para entender a estrutura do código
- Consulte [Banco de Dados](../banco-de-dados/README.md) para entender o schema
- Consulte [CI/CD](../ci-cd/README.md) para entender os pipelines
