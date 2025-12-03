# Diferenças entre Ambiente Local e Railway

Este documento detalha as diferenças de configuração entre o ambiente de desenvolvimento local (localhost) e o ambiente de produção no Railway.

## Resumo das Principais Diferenças

| Aspecto | Localhost | Railway |
|---------|-----------|---------|
| **Database** | H2 in-memory | PostgreSQL |
| **Spring Profile** | dev | prod |
| **SSL/HTTPS** | Self-signed cert (port 8443) | Railway proxy (port 8080) |
| **Frontend-Backend** | Mesma porta ou localhost:8443 | Serviços separados |
| **Build** | Gradle local | Nixpacks + Gradle |
| **Variáveis de Ambiente** | Arquivo .properties | Railway Dashboard |

## Backend - Configurações Detalhadas

### Base de Dados

**Localhost (application-dev.properties)**:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

**Railway (application-prod.properties)**:
```properties
spring.datasource.url=${DATABASE_URL:${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/slotify}}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

**Implicações**:
- ✅ Desenvolvimento usa H2 para rapidez e facilidade
- ✅ Produção usa PostgreSQL para persistência real
- ⚠️ Dados do H2 são perdidos ao reiniciar a aplicação
- ✅ Flyway garante que o schema seja o mesmo em ambos ambientes

### SSL/HTTPS

**Localhost**:
```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:slotfy.p12
server.ssl.key-store-password=slotfypass
```
- Acesso via: `https://localhost:8443`
- Certificado auto-assinado (browser mostra aviso)

**Railway**:
```properties
server.port=${PORT:8080}
server.ssl.enabled=false
server.forward-headers-strategy=native
```
- Railway termina SSL no proxy
- Acesso via: `https://seu-backend.railway.app`
- Certificado válido automático

### Flyway Migrations

**Ambos ambientes**:
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

**Comportamento**:
- Localhost: Migrations rodam no H2 a cada restart
- Railway: Migrations rodam uma vez no PostgreSQL

### Logging

**Localhost**:
```properties
logging.level.com.slotify=DEBUG
logging.level.org.springframework.web=DEBUG
spring.jpa.show-sql=true
```

**Railway**:
```properties
logging.level.root=INFO
logging.level.com.slotfy=DEBUG
logging.level.org.springframework.web=INFO
spring.jpa.show-sql=false
```

## Frontend - Configurações Detalhadas

### URL do Backend

**Localhost (api-config.js)**:
```javascript
// Detecta automaticamente:
// - Se na porta 8443: usa URLs relativas
// - Se em outra porta: usa https://localhost:8443
baseUrl: 'https://localhost:8443' // ou ''
```

**Railway**:
```javascript
// Injetado via inject-config.sh durante build:
window.BACKEND_URL = 'https://seu-backend.railway.app'
```

### Serving

**Localhost**:
- Opção 1: Servido pelo Spring Boot (static resources)
- Opção 2: Live Server / Python HTTP Server na porta 5500

**Railway**:
- Servido pelo `serve` (npm package)
- Porta fornecida pela variável `$PORT`

## Build Process

### Localhost

```bash
cd back-end
./gradlew clean build
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Railway

```toml
# nixpacks.toml
[phases.build]
cmds = ['./gradlew clean build -x test']

[start]
cmd = 'java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar build/libs/slotify-backend-0.0.1-SNAPSHOT.jar'
```

**Diferenças**:
- Railway: Testes são pulados (`-x test`) para build mais rápido
- Railway: Perfil `prod` é forçado via `-Dspring.profiles.active=prod`
- Railway: Porta é definida via variável de ambiente `$PORT`

## Variáveis de Ambiente

### Localhost

Definidas em:
- `application.properties` (base)
- `application-dev.properties` (específico)
- Ou via `-D` no comando de execução

Exemplo:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Railway

Definidas no Railway Dashboard:

**Backend**:
```
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=postgres://...
SENDGRID_API_KEY=SG.xxx
FRONTEND_URL=https://seu-frontend.railway.app
AWS_ACCESS_KEY_ID=xxx
AWS_SECRET_ACCESS_KEY=xxx
```

**Frontend**:
```
BACKEND_URL=https://seu-backend.railway.app
```

## CORS

### Configuração (mesma para ambos)

```java
registry.addMapping("/**")
    .allowedOriginPatterns("*")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(true);
```

**Comportamento**:
- Localhost: Permite requisições de qualquer origem (para desenvolvimento)
- Railway: Permite requisições de qualquer origem (configurado assim por escolha)

**Recomendação de Segurança**:
Para produção, considere restringir:
```java
.allowedOrigins(System.getenv("FRONTEND_URL"))
```

## Deploy

### Localhost

1. Pull do código
2. `./gradlew clean build`
3. `./gradlew bootRun`

### Railway

**Automático (GitHub Actions)**:
1. Push para branch `main`
2. GitHub Actions aciona workflow
3. Railway CLI faz deploy dos serviços

**Manual**:
```bash
railway login
railway up --service "TCC_FHO: Back-end"
railway up --service "TCC_FHO: Front-end"
```

## Checklist de Sincronização

Para garantir que localhost e Railway executem a mesma versão:

### Código
- [x] Mesmo branch (geralmente `main`)
- [x] Mesma versão do código (sem commits locais não pushados)

### Configuração
- [x] Profiles Spring corretos (dev vs prod)
- [x] Variáveis de ambiente configuradas
- [x] URLs de frontend/backend corretas

### Database
- [x] Migrations Flyway aplicadas
- [x] Schema idêntico (H2 e PostgreSQL seguem mesmo schema)

### Build
- [x] Gradle version consistente
- [x] Java 17 em ambos ambientes
- [x] Dependências atualizadas

### Funcionalidades
- [x] Mesmas features habilitadas
- [x] Mesma lógica de negócio
- [x] Mesmos endpoints de API

## Problemas Comuns e Soluções

### 1. "Funciona no localhost mas não no Railway"

**Possíveis causas**:
- Profile Spring incorreto (deve ser `prod` no Railway)
- Variáveis de ambiente faltando
- CORS bloqueando requisições
- URL do backend incorreta no frontend

**Solução**:
```bash
# Verifique logs no Railway
railway logs --service "TCC_FHO: Back-end"

# Verifique variáveis de ambiente
railway variables --service "TCC_FHO: Back-end"
```

### 2. "Database connection error no Railway"

**Causa**: `DATABASE_URL` não configurada ou incorreta

**Solução**:
1. Verificar se PostgreSQL está conectado ao serviço
2. Verificar formato da `DATABASE_URL`
3. Testar conexão manualmente

### 3. "Frontend não conecta ao backend no Railway"

**Causa**: `BACKEND_URL` não configurada no frontend

**Solução**:
1. Adicionar variável `BACKEND_URL` no Railway (frontend service)
2. Verificar que `inject-config.sh` está rodando no build
3. Verificar CORS no backend

### 4. "Build falha no Railway"

**Possíveis causas**:
- Root directory incorreto
- `nixpacks.toml` não encontrado
- Erro no Gradle

**Solução**:
1. Verificar root directory: `back-end` ou `front-end`
2. Verificar que `nixpacks.toml` existe
3. Testar build localmente: `./gradlew clean build -x test`

## Resumo de Comandos Úteis

### Localhost
```bash
# Backend
cd back-end
./gradlew clean build
./gradlew bootRun --args='--spring.profiles.active=dev'

# Frontend
cd front-end/src
python3 -m http.server 5500
```

### Railway
```bash
# Login
railway login

# Ver logs
railway logs --service "TCC_FHO: Back-end"
railway logs --service "TCC_FHO: Front-end"

# Ver variáveis
railway variables --service "TCC_FHO: Back-end"

# Deploy manual
railway up --service "TCC_FHO: Back-end"
railway up --service "TCC_FHO: Front-end"

# Abrir no browser
railway open
```
