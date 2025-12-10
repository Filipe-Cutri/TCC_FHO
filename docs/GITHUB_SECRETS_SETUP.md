# GitHub Secrets Configuration Guide

## Overview

This guide explains how to configure GitHub Secrets for automatic deployment to Railway, ensuring that password recovery emails and other production features work correctly.

## Required Secrets

### 1. RAILWAY_TOKEN

**Purpose**: Authenticates GitHub Actions to deploy to Railway

**How to obtain**:
1. Go to [Railway.app](https://railway.app)
2. Click on your profile → Account Settings
3. Navigate to "Tokens" section
4. Click "Create Token"
5. Give it a name like "GitHub Actions Deploy"
6. Copy the generated token

**How to set in GitHub**:
1. Go to your GitHub repository
2. Click on "Settings" → "Secrets and variables" → "Actions"
3. Click "New repository secret"
4. Name: `RAILWAY_TOKEN`
5. Value: Paste the token you copied
6. Click "Add secret"

### 2. FRONTEND_URL

**Purpose**: URL of the frontend application, used in password recovery emails and backend redirects

**Value format**: `https://your-frontend-domain.com` (without trailing slash)

**Examples**:
- Production: `https://slotfy.com.br`
- Railway default: `https://your-project-production.up.railway.app`

**How to set in GitHub**:
1. Go to your GitHub repository
2. Click on "Settings" → "Secrets and variables" → "Actions"
3. Click "New repository secret"
4. Name: `FRONTEND_URL`
5. Value: Your frontend URL (e.g., `https://slotfy.com.br`)
6. Click "Add secret"

**⚠️ CRITICAL**: This URL is used in password recovery emails. If not set correctly, users won't be able to reset their passwords in production!

### 3. BACKEND_URL

**Purpose**: URL of the backend API, used by the frontend to make API calls

**Value format**: `https://your-backend-domain.com` (without trailing slash)

**Examples**:
- Production: `https://api.slotfy.com.br`
- Railway default: `https://your-backend-production.up.railway.app`

**How to set in GitHub**:
1. Go to your GitHub repository
2. Click on "Settings" → "Secrets and variables" → "Actions"
3. Click "New repository secret"
4. Name: `BACKEND_URL`
5. Value: Your backend URL (e.g., `https://api.slotfy.com.br`)
6. Click "Add secret"

## Verification

After setting up the secrets, you can verify they're configured correctly:

1. Go to "Settings" → "Secrets and variables" → "Actions"
2. You should see all three secrets listed:
   - ✅ RAILWAY_TOKEN
   - ✅ FRONTEND_URL
   - ✅ BACKEND_URL

## How It Works

When you push to the `main` branch or manually trigger the deploy workflow:

1. GitHub Actions reads the secrets
2. Connects to Railway using `RAILWAY_TOKEN`
3. Sets `FRONTEND_URL` on the backend service
4. Sets `BACKEND_URL` on the frontend service
5. Triggers redeployment of both services

### Backend Service

The backend uses `FRONTEND_URL` to generate links in emails:
```java
// In application.properties
frontend.url=${FRONTEND_URL:https://localhost:8443}

// In ForgotPasswordService.java
String resetLink = frontendUrl + "/pages/reset-password.html?email=" + email + "&token=" + rawToken;
```

### Frontend Service

The frontend uses `BACKEND_URL` for API calls. The `inject-config.sh` script generates `config.js` during deployment:
```javascript
// Generated config.js
window.BACKEND_URL = 'https://api.slotfy.com.br';
```

## Local Development

For local development, you don't need to set these secrets. The default values work:

- **Backend**: `https://localhost:8443` (frontend URL)
- **Frontend**: Manually set in `front-end/src/config.js`

## Troubleshooting

### Problem: Password recovery emails contain localhost URLs

**Cause**: `FRONTEND_URL` secret is not configured in GitHub

**Solution**:
1. Set the `FRONTEND_URL` secret as described above
2. Manually set in Railway: Backend Service → Variables → Add `FRONTEND_URL`
3. Redeploy the backend service

### Problem: Frontend can't connect to backend

**Cause**: `BACKEND_URL` secret is not configured in GitHub

**Solution**:
1. Set the `BACKEND_URL` secret as described above
2. Manually set in Railway: Frontend Service → Variables → Add `BACKEND_URL`
3. Redeploy the frontend service

### Problem: GitHub Actions fails with "RAILWAY_TOKEN is invalid"

**Cause**: Token is expired or incorrect

**Solution**:
1. Generate a new token in Railway
2. Update the `RAILWAY_TOKEN` secret in GitHub
3. Retry the workflow

## Manual Railway Configuration (Alternative)

If you prefer not to use GitHub Actions, you can manually configure these variables in Railway:

### Backend Service
1. Go to Railway → Backend Service → Variables
2. Add `FRONTEND_URL` = `https://your-frontend.com`
3. Click "Deploy"

### Frontend Service
1. Go to Railway → Frontend Service → Variables
2. Add `BACKEND_URL` = `https://your-backend.com`
3. Click "Deploy"

## Security Best Practices

1. ✅ Never commit secrets to the repository
2. ✅ Use GitHub Secrets for sensitive values
3. ✅ Rotate RAILWAY_TOKEN periodically
4. ✅ Use HTTPS URLs for both frontend and backend
5. ✅ Don't include trailing slashes in URLs

## Related Documentation

- [Railway Setup Guide](../RAILWAY_SETUP.md)
- [Deployment Guide](../DEPLOYMENT_GUIDE.md)
- [Password Recovery Guide](PASSWORD_RECOVERY_GUIDE.md)
