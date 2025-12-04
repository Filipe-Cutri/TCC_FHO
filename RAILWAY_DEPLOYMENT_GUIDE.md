# Railway Deployment & Version Management Guide

## Overview

This guide explains how to verify deployed versions, debug deployment issues, and understand the version tracking system implemented in the TCC_FHO project.

## Version Tracking System

### Backend Version Information

The backend exposes version information through the `/api/health` endpoint.

**Endpoint:** `GET /api/health`

**Response Format:**
```json
{
  "status": "ok",
  "timestamp": "2025-12-04T20:00:00.000Z",
  "service": "backend",
  "version": "v0.0.37",
  "commit": "b262350",
  "environment": "production",
  "uptime": 123456,
  "memory": {
    "total": 536870912,
    "free": 268435456,
    "used": 268435456,
    "max": 1073741824
  }
}
```

### Frontend Version Information

The frontend exposes version information through a static JSON file and console logs.

**File:** `GET /version.json`

**Response Format:**
```json
{
  "service": "frontend",
  "version": "v0.0.37",
  "commit": "b262350",
  "buildDate": "2025-12-04T20:00:00Z",
  "environment": "production"
}
```

**Browser Console:**
When you open the frontend in a browser, check the console (F12) to see version information automatically logged on page load.

## Checking Deployed Versions

### Quick Check Script

You can use the following bash script to check both frontend and backend versions:

```bash
#!/bin/bash
# check-versions.sh - Check deployed versions

BACKEND_URL="${1:-https://tccfho-production.up.railway.app}"
FRONTEND_URL="${2:-https://tccfho-production-baff.up.railway.app}"

echo "🔍 Checking deployed versions..."
echo ""

# Check Backend
echo "📦 BACKEND ($BACKEND_URL)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
BACKEND_RESPONSE=$(curl -s "$BACKEND_URL/api/health" || echo '{"error":"Failed to connect"}')
echo "$BACKEND_RESPONSE" | jq '.' 2>/dev/null || echo "$BACKEND_RESPONSE"
echo ""

# Check Frontend
echo "🎨 FRONTEND ($FRONTEND_URL)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
FRONTEND_RESPONSE=$(curl -s "$FRONTEND_URL/version.json" || echo '{"error":"Failed to connect"}')
echo "$FRONTEND_RESPONSE" | jq '.' 2>/dev/null || echo "$FRONTEND_RESPONSE"
echo ""

# Summary
echo "✅ Version check complete!"
```

**Usage:**
```bash
chmod +x check-versions.sh
./check-versions.sh
```

### Manual Checks

**Check Backend:**
```bash
curl https://tccfho-production.up.railway.app/api/health | jq
```

**Check Frontend:**
```bash
curl https://tccfho-production-baff.up.railway.app/version.json | jq
```

## Deployment Process

### Automatic Deployment

When you push to the `main` branch:

1. **Release Workflow** (`release.yml`) creates a new version tag automatically
2. **Deploy Workflow** (`deploy.yml`) extracts version information and sets Railway environment variables
3. Railway automatically redeploys services when environment variables change

### Environment Variables Set by Deploy Workflow

For both backend and frontend services, the following environment variables are set:

- `APP_VERSION`: Git tag version (e.g., `v0.0.37`)
- `COMMIT_HASH`: Short commit hash (e.g., `b262350`)
- `BUILD_DATE`: ISO 8601 timestamp of build

### Manual Deployment

To manually trigger a deployment with version tracking:

1. Go to GitHub Actions
2. Select "Deploy to Railway" workflow
3. Click "Run workflow"
4. Select the branch to deploy

## Debugging Deployment Issues

### Backend Issues

**Problem: 500 Internal Server Error on /health**

**Checklist:**
1. ✅ Check Railway logs for the backend service
2. ✅ Verify database connection (DATABASE_URL is set)
3. ✅ Check that environment variables are set: `APP_VERSION`, `COMMIT_HASH`
4. ✅ Verify Spring profile is set to `prod`
5. ✅ Check for Java exceptions in logs

**Railway CLI Commands:**
```bash
# View backend logs
railway logs --service backend

# Check environment variables
railway variables --service backend

# Restart backend service
railway up --service backend
```

### Frontend Issues

**Problem: 502 Bad Gateway**

**Checklist:**
1. ✅ Check if the build phase completed successfully
2. ✅ Verify `inject-config.sh` ran during build
3. ✅ Check that `serve` is installed and running
4. ✅ Verify PORT environment variable is set
5. ✅ Check BACKEND_URL is configured

**Railway CLI Commands:**
```bash
# View frontend logs
railway logs --service frontend

# Check environment variables
railway variables --service frontend

# Restart frontend service
railway up --service frontend
```

### Common Issues and Solutions

#### Issue: Version shows "unknown"

**Cause:** Environment variables not set in Railway

**Solution:**
1. Run the deploy workflow to set variables
2. Or manually set via Railway dashboard:
   - Go to service settings → Variables
   - Add: `APP_VERSION`, `COMMIT_HASH`, `BUILD_DATE`

#### Issue: Health endpoint returns 500

**Cause:** Application error or database connection issue

**Solution:**
1. Check logs: `railway logs --service backend`
2. Verify DATABASE_URL is set
3. Check Spring Boot startup logs for errors

#### Issue: Frontend shows old version

**Cause:** Browser cache or deployment not completed

**Solution:**
1. Hard refresh browser (Ctrl+Shift+R or Cmd+Shift+R)
2. Check Railway deployment status
3. Verify version.json was updated: `curl https://[url]/version.json`

## Local Development

### Running Backend Locally

```bash
cd back-end
./gradlew bootRun
```

Access health endpoint: `http://localhost:8080/api/health`

### Running Frontend Locally

```bash
cd front-end
npx serve -s src -l 3000
```

Access: `http://localhost:3000`

## Railway Configuration Files

### Backend: `back-end/nixpacks.toml`

```toml
[phases.setup]
nixPkgs = ['openjdk17']

[phases.build]
cmds = ['./gradlew clean build -x test']

[start]
cmd = 'java -Dserver.port=$PORT -Dspring.profiles.active=prod -Dapp.version=${APP_VERSION:-unknown} -Dapp.commit.hash=${COMMIT_HASH:-unknown} -jar build/libs/slotify-backend-*.jar'
```

### Frontend: `front-end/nixpacks.toml`

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

## Monitoring

### Health Checks

Set up monitoring to ping these endpoints regularly:

- Backend: `https://tccfho-production.up.railway.app/api/health`
- Frontend: `https://tccfho-production-baff.up.railway.app/version.json`

Expected status codes:
- 200: Service is healthy
- 500: Backend error (check logs)
- 502: Service not running or misconfigured
- 503: Service starting up

### Uptime Monitoring

Consider using services like:
- UptimeRobot
- Pingdom
- StatusCake

## Troubleshooting Checklist

### Pre-Deployment
- [ ] Code builds successfully locally
- [ ] Tests pass
- [ ] Environment variables documented
- [ ] Database migrations ready (if any)

### Post-Deployment
- [ ] Backend health endpoint returns 200
- [ ] Frontend loads successfully
- [ ] Version information matches deployed commit
- [ ] Database connection working
- [ ] No errors in Railway logs

## Support

If issues persist:

1. Check Railway status: https://status.railway.app/
2. Review Railway logs for both services
3. Verify all required environment variables are set
4. Check GitHub Actions workflow runs for errors
5. Review this documentation for common issues

## Related Files

- `.github/workflows/deploy.yml` - Deployment automation
- `.github/workflows/release.yml` - Version tagging
- `back-end/src/main/java/com/slotfy/controller/HealthController.java` - Health endpoint
- `front-end/inject-config.sh` - Frontend config injection
- `front-end/src/version.json` - Frontend version file
