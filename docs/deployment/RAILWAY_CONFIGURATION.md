# Railway Configuration Guide

This guide explains how to configure the Slotfy application on Railway with separate frontend and backend services.

> **⚠️ Problema com Deploy Automático?** Se você está tendo problemas com o deploy automático do GitHub Actions para o Railway, veja [Railway Deployment Fix](./RAILWAY_DEPLOYMENT_FIX.md) para uma solução completa.

> **🚨 IMPORTANTE: Configuração Multi-Service**  
> Este projeto usa **TRÊS serviços separados** no Railway:
> - **Backend Service** (root directory: `back-end`)
> - **Frontend Service** (root directory: `front-end`)  
> - **PostgreSQL Database**
>
> **NÃO deve existir** um arquivo `nixpacks.toml` na raiz do repositório. Cada serviço usa seu próprio `nixpacks.toml` dentro de seu diretório raiz.

## Prerequisites

- Railway account
- GitHub repository connected to Railway
- Railway CLI installed (for local testing)

## Project Structure on Railway

The Slotfy application should be deployed as **three separate services** in Railway:

1. **Backend Service** - Spring Boot application
2. **Frontend Service** - Static HTML/JS application
3. **PostgreSQL Database** - Database service

## Backend Service Configuration

### Root Directory
Set the root directory to: `back-end`

### Environment Variables

Configure the following environment variables in Railway for the backend service:

#### Required Database Variables

Railway will automatically provide these when you attach a PostgreSQL database:
- `DATABASE_URL` - Full PostgreSQL connection URL (format: `postgres://user:password@host:port/database`)

Or you can set them manually:
- `SPRING_DATASOURCE_URL` - JDBC URL (format: `jdbc:postgresql://host:port/database`)
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

#### Required Spring Configuration
- `SPRING_PROFILES_ACTIVE=prod` - Activates production profile
- `PORT` - Automatically provided by Railway (typically 8080)

#### Email Configuration (SendGrid)
- `SENDGRID_API_KEY` - Your SendGrid API key
- `SENDGRID_FROM` - From email address (e.g., `noreply@slotfy.com`)

#### Frontend URL
- `FRONTEND_URL` - URL of your frontend service (e.g., `https://your-frontend.railway.app`)

#### AWS Bedrock Configuration (Optional - for AI features)
- `AWS_REGION` - AWS region (default: `us-east-1`)
- `AWS_ACCESS_KEY_ID` - AWS access key
- `AWS_SECRET_ACCESS_KEY` - AWS secret key
- `BEDROCK_MODEL_ID` - Bedrock model ID (default: `meta.llama3-70b-instruct-v1:0`)

### Build Configuration

> **⚠️ IMPORTANTE:** O arquivo `nixpacks.toml` do backend está localizado em `back-end/nixpacks.toml`, **NÃO** na raiz do repositório.  
> Railway usa a configuração de "Root Directory" (`back-end`) para encontrar este arquivo automaticamente.

The backend uses `nixpacks.toml` for build configuration. Railway will automatically detect and use this.

**File**: `back-end/nixpacks.toml`

```toml
[phases.setup]
nixPkgs = ['openjdk17']

[phases.build]
cmds = ['./gradlew clean build -x test']

[start]
cmd = 'java -Dserver.port=$PORT -Dspring.profiles.active=prod -Dapp.version=${APP_VERSION:-unknown} -Dapp.commit.hash=${COMMIT_HASH:-unknown} -jar build/libs/slotify-backend-*.jar'
```

**Observações Importantes:**
- O comando usa `slotify-backend-*.jar` com wildcard para compatibilidade com diferentes versões
- As variáveis `APP_VERSION` e `COMMIT_HASH` são injetadas pelo workflow de deploy do GitHub Actions
- O profile `prod` é obrigatório para configuração de produção

### Health Check (Optional)
- Path: `/actuator/health` (if Spring Boot Actuator is enabled)
- Or: `/api/health` (if you have a custom health endpoint)

## Frontend Service Configuration

### Root Directory
Set the root directory to: `front-end`

### Environment Variables

Configure the following environment variables in Railway for the frontend service:

#### Required
- `BACKEND_URL` - Full URL of your backend service (e.g., `https://your-backend.railway.app`)
- `PORT` - Automatically provided by Railway

### Build Configuration

> **⚠️ IMPORTANTE:** O arquivo `nixpacks.toml` do frontend está localizado em `front-end/nixpacks.toml`, **NÃO** na raiz do repositório.  
> Railway usa a configuração de "Root Directory" (`front-end`) para encontrar este arquivo automaticamente.

The frontend uses `nixpacks.toml` for build configuration.

**File**: `front-end/nixpacks.toml`

```toml
[phases.setup]
nixPkgs = ['nodejs', 'bash']

[phases.install]
cmds = ['npm install -g serve']

[phases.build]
cmds = ['chmod +x inject-config.sh && ./inject-config.sh']

[start]
cmd = 'serve -s src -l $PORT'
```

The `inject-config.sh` script will automatically inject the `BACKEND_URL` into the frontend configuration.

**Observações Importantes:**
- O script `inject-config.sh` cria dinamicamente os arquivos `config.js` e `version.json` durante o build
- A variável `BACKEND_URL` é obrigatória e deve apontar para o serviço backend
- As variáveis `APP_VERSION` e `COMMIT_HASH` são injetadas pelo workflow de deploy do GitHub Actions

## Database Setup

1. Create a PostgreSQL database in Railway
2. Connect it to your backend service
3. Railway will automatically set the `DATABASE_URL` environment variable
4. The application will run Flyway migrations on startup

## GitHub Actions Integration

The repository includes a deploy workflow that automatically deploys both services when code is pushed to the `main` branch.

**File**: `.github/workflows/deploy.yml`

### Required GitHub Secrets

Add the following secret to your GitHub repository:
- `RAILWAY_TOKEN` - Your Railway API token (get it from Railway dashboard)

### How it works

1. Push code to `main` branch
2. GitHub Actions triggers the deploy workflow
3. Backend service is deployed first
4. Frontend service is deployed after backend completes

### Service Name Resolution

The workflow uses a fallback strategy to find the correct service names:
1. Tries the documented format (e.g., `"TCC_FHO: Front-end"`)
2. Tries lowercase with hyphens (e.g., `"tcc-fho-front-end"`)
3. Tries simplified names (e.g., `"frontend"`)
4. Falls back to auto-detection based on working directory

> **💡 Tip**: If deployments are failing, see [Railway Deployment Fix](./RAILWAY_DEPLOYMENT_FIX.md) for detailed troubleshooting steps.

## CORS Configuration

The backend is configured to allow CORS requests from any origin in production. This is set in `WebConfig.java`:

```java
registry.addMapping("/**")
    .allowedOriginPatterns("*")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(true);
```

If you want to restrict CORS to specific origins, update the configuration to use your frontend URL:

```java
.allowedOrigins(System.getenv("FRONTEND_URL"))
```

## SSL/HTTPS Configuration

- **Development**: Uses self-signed certificate (configured in `application-dev.properties`)
- **Production**: Railway automatically provides HTTPS with valid SSL certificates

The backend `application-prod.properties` has:
```properties
server.ssl.enabled=false
server.forward-headers-strategy=native
```

This is correct because Railway terminates SSL at the proxy level.

## Verification Steps

After deployment, verify:

1. **Backend Health**:
   ```bash
   curl https://your-backend.railway.app/api/health
   ```

2. **Frontend Access**:
   - Open `https://your-frontend.railway.app` in browser
   - Check browser console for any API connection errors

3. **Database Connection**:
   - Check Railway logs for successful Flyway migrations
   - Verify no connection errors in backend logs

## Troubleshooting

### Backend won't start
- Check Railway logs for errors
- Verify `DATABASE_URL` is set correctly
- Ensure `SPRING_PROFILES_ACTIVE=prod` is set

### Frontend can't connect to backend
- Verify `BACKEND_URL` environment variable is set in frontend service
- Check browser console for CORS errors
- Ensure backend URL is correct (including https://)

### Database connection errors
- Verify PostgreSQL service is running
- Check `DATABASE_URL` format is correct
- Ensure database credentials are valid

### Build failures
- Check that root directories are set correctly (`back-end` for backend, `front-end` for frontend)
- Verify `nixpacks.toml` files exist in the correct locations (inside each service directory, NOT in repository root)
- Review Railway build logs for specific errors
- **IMPORTANTE:** Certifique-se de que NÃO existe um arquivo `nixpacks.toml` na raiz do repositório

### Railway detecta o arquivo errado
**Sintoma:** Railway tenta construir a partir do diretório errado

**Causa:** Existe um `nixpacks.toml` na raiz do repositório interferindo com a configuração multi-service

**Solução:**
1. Delete o arquivo `nixpacks.toml` da raiz do repositório (se existir)
2. Mantenha apenas:
   - `back-end/nixpacks.toml` para o serviço backend
   - `front-end/nixpacks.toml` para o serviço frontend
3. Configure a "Root Directory" corretamente em cada serviço Railway:
   - Backend Service → Settings → Root Directory: `back-end`
   - Frontend Service → Settings → Root Directory: `front-end`

## Local Development

For local development, use:

**Backend**:
```bash
cd back-end
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Frontend**:
Open `front-end/src/index.html` in a browser or serve with:
```bash
cd front-end/src
python3 -m http.server 5500
```

The frontend will automatically detect it's running on localhost and connect to `https://localhost:8443`.

## Migration from Localhost to Railway

1. **Set up Railway project** with two services (frontend and backend)
2. **Configure environment variables** as described above
3. **Connect GitHub repository** to Railway
4. **Set root directories** for each service
5. **Deploy** manually or push to `main` branch for automatic deployment
6. **Update DNS** if using custom domain

## Additional Resources

- [Railway Documentation](https://docs.railway.app/)
- [Spring Boot Production Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
- [Nixpacks Documentation](https://nixpacks.com/)
