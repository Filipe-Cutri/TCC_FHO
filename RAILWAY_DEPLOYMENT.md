# Railway Deployment Guide - Slotify Backend

This guide explains how to deploy the Slotify backend application to Railway.

## Prerequisites

- Railway account (sign up at https://railway.app)
- PostgreSQL database provisioned on Railway
- SendGrid API key for email functionality

## Environment Variables

Configure the following environment variables in your Railway project:

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring Boot profile to use | `prod` |
| `DATABASE_URL` | PostgreSQL connection URL | Auto-provided by Railway PostgreSQL |
| `PGUSER` | PostgreSQL username | Auto-provided by Railway PostgreSQL |
| `PGPASSWORD` | PostgreSQL password | Auto-provided by Railway PostgreSQL |
| `SENDGRID_API_KEY` | SendGrid API key for email | `SG.xxxxxxxxxxxxxx` |
| `PORT` | Server port | Auto-provided by Railway |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SENDGRID_FROM_EMAIL` | Email sender address | `noreply@slotfy.com` |
| `SENDGRID_FROM_NAME` | Email sender name | `Slotfy - Sistema de Agendamento` |
| `FILE_UPLOAD_DIR` | Directory for file uploads | `/tmp/uploads` |
| `MAX_FILE_SIZE` | Maximum file upload size | `5MB` |
| `MAX_REQUEST_SIZE` | Maximum request size | `5MB` |
| `JPA_DDL_AUTO` | Hibernate DDL auto mode | `validate` |
| `JPA_SHOW_SQL` | Show SQL queries in logs | `false` |

## Deployment Steps

### 1. Create a New Project on Railway

1. Log in to Railway
2. Click "New Project"
3. Select "Deploy from GitHub repo"
4. Choose the `Filipe-Cutri/TCC_FHO` repository

### 2. Add PostgreSQL Database

1. In your Railway project, click "New"
2. Select "Database" → "Add PostgreSQL"
3. Railway will automatically create and provide `DATABASE_URL`, `PGUSER`, and `PGPASSWORD`

### 3. Configure Environment Variables

1. Go to your service settings
2. Navigate to "Variables" tab
3. Add the required environment variables:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `SENDGRID_API_KEY=your_sendgrid_api_key`
   - Any other optional variables as needed

### 4. Deploy

Railway will automatically:
1. Detect the Java/Gradle project
2. Run the build command specified in `railway.json`
3. Execute the start command to run the application
4. Assign a public URL to your service

### 5. Database Migration

Before the application can run, ensure your PostgreSQL database has the correct schema:

1. Connect to your Railway PostgreSQL database
2. Run the SQL migration scripts located in the repository root:
   - `database_schema.sql`
   - `database_migration_notifications_payments.sql`
   - `database_migration_add_establishment_to_client.sql`

You can connect to the database using the credentials from Railway's PostgreSQL service.

## Build Configuration

The project supports two deployment methods:

### Method 1: Nixpacks (Recommended)

Railway will automatically detect and build using Nixpacks:

- **Build Command**: `cd back-end && ./gradlew clean build -x test`
- **Start Command**: `cd back-end && java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar build/libs/*.jar`

Configuration files:
- `railway.json` - Railway-specific settings
- `nixpacks.toml` - Nixpacks build configuration
- `Procfile` - Process definition

### Method 2: Docker

Alternatively, you can use Docker for deployment:

- **Dockerfile**: Multi-stage Docker build
- Build: `docker build -t slotify-backend .`
- Run: `docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod slotify-backend`

Railway will automatically detect the Dockerfile if present and offer to use it.

## Profiles

The application supports multiple profiles:

- **dev**: Development profile using H2 in-memory database with SSL enabled
- **test**: Testing profile using H2 in-memory database
- **prod**: Production profile using PostgreSQL (recommended for Railway)

## Security Notes

✅ All sensitive credentials are managed through environment variables
✅ No API keys or passwords are hardcoded in the source code
✅ SSL/HTTPS is managed by Railway's infrastructure
✅ Database credentials are auto-injected by Railway

## Troubleshooting

### Build Failures

If the build fails:
1. Check the build logs in Railway
2. Ensure all Gradle wrapper files are committed
3. Verify Java 17 is being used

### Runtime Errors

If the application fails to start:
1. Check that `SPRING_PROFILES_ACTIVE=prod` is set
2. Verify PostgreSQL database is running and accessible
3. Ensure all required environment variables are configured
4. Check application logs for specific error messages

### Database Connection Issues

If database connection fails:
1. Verify PostgreSQL service is running in Railway
2. Check that `DATABASE_URL`, `PGUSER`, and `PGPASSWORD` are set
3. Ensure database schema has been initialized with migration scripts

## Support

For issues specific to Railway deployment, consult:
- [Railway Documentation](https://docs.railway.app)
- [Railway Discord Community](https://discord.gg/railway)

For application-specific issues, refer to the main project README.
