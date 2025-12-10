# Password Recovery Email URL Fix - Test Plan

## Overview

This document describes how to test that password recovery emails work correctly in both localhost and production environments.

## Test Scenarios

### Scenario 1: Localhost Development Environment

**Objective**: Verify that password recovery works locally without any environment variables set.

**Setup**:
- No `FRONTEND_URL` environment variable set
- Backend runs on `https://localhost:8443`
- Frontend runs on `https://localhost:8443` or via local web server

**Steps**:
1. Start the backend server locally:
   ```bash
   cd back-end
   ./gradlew bootRun
   ```

2. Navigate to the forgot password page for either client or establishment

3. Request a password reset by entering an email address

4. Check the email received

**Expected Result**:
- Email should contain link: `https://localhost:8443/pages/reset-password.html?email=...&token=...`
- Link should work when clicked locally

**Validation**:
- ✅ Email is sent successfully
- ✅ Link contains `localhost:8443`
- ✅ Link works when clicked
- ✅ Password reset functionality works end-to-end

---

### Scenario 2: Production Environment (Railway) - Manual Configuration

**Objective**: Verify that password recovery works in production with manually configured environment variables.

**Setup**:
1. In Railway Backend Service → Variables:
   - Set `FRONTEND_URL` = `https://slotfy.com.br` (or your production frontend URL)
   
2. In Railway Frontend Service → Variables:
   - Set `BACKEND_URL` = `https://api.slotfy.com.br` (or your production backend URL)

3. Redeploy both services

**Steps**:
1. Navigate to production forgot password page

2. Request a password reset by entering an email address

3. Check the email received

**Expected Result**:
- Email should contain link: `https://slotfy.com.br/pages/reset-password.html?email=...&token=...`
- Link should work when clicked in production

**Validation**:
- ✅ Email is sent successfully
- ✅ Link contains production frontend URL (NOT localhost)
- ✅ Link works when clicked from any device
- ✅ Password reset functionality works end-to-end

---

### Scenario 3: Production Environment (Railway) - Automated via GitHub Actions

**Objective**: Verify that environment variables are set automatically via GitHub Actions deployment.

**Setup**:
1. Configure GitHub Secrets (see [GitHub Secrets Setup Guide](docs/GITHUB_SECRETS_SETUP.md)):
   - `RAILWAY_TOKEN`: Railway API token
   - `FRONTEND_URL`: `https://slotfy.com.br`
   - `BACKEND_URL`: `https://api.slotfy.com.br`

2. Push to `main` branch or manually trigger deploy workflow

**Steps**:
1. Go to GitHub Actions → Deploy to Railway workflow

2. Check the workflow run logs

3. Verify that the summary shows:
   - ✅ FRONTEND_URL: Configured ✅
   - ✅ BACKEND_URL: Configured ✅

4. Wait for Railway deployment to complete

5. Test password recovery in production (same as Scenario 2)

**Expected Result**:
- Workflow completes successfully
- Environment variables are set in Railway
- Password recovery emails contain production URLs

**Validation**:
- ✅ GitHub Actions workflow succeeds
- ✅ Railway environment variables are updated
- ✅ Password recovery works in production
- ✅ Email links contain production URLs

---

## Test Matrix

| Environment | FRONTEND_URL | BACKEND_URL | Expected Email Link |
|-------------|--------------|-------------|---------------------|
| Localhost | (not set) | (not set) | `https://localhost:8443/pages/reset-password.html` |
| Production (Manual) | Set in Railway | Set in Railway | `https://slotfy.com.br/pages/reset-password.html` |
| Production (Automated) | Set via GitHub Secret | Set via GitHub Secret | `https://slotfy.com.br/pages/reset-password.html` |

---

## Verification Commands

### Check Backend Configuration in Railway

```bash
# Using Railway CLI
railway variables --service backend | grep FRONTEND_URL
```

### Check Frontend Configuration in Railway

```bash
# Using Railway CLI
railway variables --service frontend | grep BACKEND_URL
```

### Verify Frontend config.js in Production

```bash
# Check the injected configuration
curl https://slotfy.com.br/config.js
```

Expected output:
```javascript
window.BACKEND_URL = 'https://api.slotfy.com.br';
```

### Check Backend Health Endpoint

```bash
curl https://api.slotfy.com.br/api/health
```

---

## Troubleshooting

### Issue: Email still contains localhost URL in production

**Possible Causes**:
1. `FRONTEND_URL` not set in Railway
2. Backend service not redeployed after setting variable
3. GitHub Secret not configured

**Solutions**:
1. Verify Railway variable: `railway variables --service backend | grep FRONTEND_URL`
2. Manually set in Railway: Backend Service → Variables → Add `FRONTEND_URL`
3. Redeploy backend service
4. Check GitHub Secrets: Settings → Secrets and variables → Actions

### Issue: Frontend can't connect to backend

**Possible Causes**:
1. `BACKEND_URL` not set in Railway
2. `config.js` not properly injected during build
3. CORS issues

**Solutions**:
1. Verify Railway variable: `railway variables --service frontend | grep BACKEND_URL`
2. Check `config.js`: `curl https://your-frontend.com/config.js`
3. Verify `inject-config.sh` ran during build (check Railway build logs)
4. Redeploy frontend service

### Issue: GitHub Actions fails to set variables

**Possible Causes**:
1. Invalid `RAILWAY_TOKEN`
2. Service names don't match ('backend' and 'frontend')
3. Network issues

**Solutions**:
1. Generate new Railway token and update GitHub Secret
2. Check Railway service names match exactly
3. Retry the workflow
4. Check Railway status: https://status.railway.app/

---

## Success Criteria

The fix is considered successful when:

- ✅ Local development works with default localhost URLs
- ✅ Production emails contain production frontend URLs (NOT localhost)
- ✅ GitHub Actions automatically sets environment variables
- ✅ Password recovery works end-to-end in both environments
- ✅ Documentation is clear and complete
- ✅ No manual intervention required for each deployment (when using GitHub Actions)

---

## Related Documentation

- [GitHub Secrets Setup Guide](GITHUB_SECRETS_SETUP.md)
- [Deployment Guide](../DEPLOYMENT_GUIDE.md)
- [Railway Setup Guide](../RAILWAY_SETUP.md)
- [Password Recovery Guide](PASSWORD_RECOVERY_GUIDE.md)
