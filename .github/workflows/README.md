# GitHub Actions Workflows

## Overview

This directory contains GitHub Actions workflows for automating deployment and release processes.

## Workflows

### 1. Deploy to Railway (`deploy.yml`)

Automatically deploys the application to Railway when changes are pushed to the `main` branch.

#### Triggers
- Push to `main` branch
- Push of version tags (`v*.*.*`)
- Manual workflow dispatch

#### What it does
1. **Extracts Version Information**
   - Gets the latest Git tag
   - Extracts commit hash
   - Generates build timestamp

2. **Sets Backend Environment Variables**
   - `APP_VERSION`: Version from Git tag
   - `COMMIT_HASH`: Short commit hash
   - `BUILD_DATE`: ISO 8601 build timestamp
   - `FRONTEND_URL`: Frontend URL (from GitHub Secret)

3. **Sets Frontend Environment Variables**
   - `APP_VERSION`: Version from Git tag
   - `COMMIT_HASH`: Short commit hash
   - `BUILD_DATE`: ISO 8601 build timestamp
   - `BACKEND_URL`: Backend API URL (from GitHub Secret)

4. **Triggers Railway Deployment**
   - Redeploys both backend and frontend services

#### Required GitHub Secrets

| Secret | Description | Example |
|--------|-------------|---------|
| `RAILWAY_TOKEN` | Railway API token for authentication | `***` |
| `FRONTEND_URL` | Production frontend URL | `https://slotfy.com.br` |
| `BACKEND_URL` | Production backend API URL | `https://api.slotfy.com.br` |

**Setup Guide**: See [Configuração](../../docs/configuracao/README.md)

#### How to Run Manually

1. Go to the "Actions" tab in GitHub
2. Select "Deploy to Railway" workflow
3. Click "Run workflow"
4. Select the branch to deploy (usually `main`)
5. Click "Run workflow"

### 2. Release (`release.yml`)

Creates version tags automatically based on commit messages.

#### Triggers
- Push to `main` branch (after deploy workflow)

#### What it does
- Analyzes commit messages
- Creates new version tags following semantic versioning
- Updates version numbers automatically

## Environment Variables in Railway

After the deploy workflow runs, the following environment variables are set in Railway:

### Backend Service
- `APP_VERSION`: Current version (e.g., `v0.0.37`)
- `COMMIT_HASH`: Git commit hash (e.g., `b262350`)
- `BUILD_DATE`: Build timestamp
- `FRONTEND_URL`: Frontend URL for email links

### Frontend Service
- `APP_VERSION`: Current version
- `COMMIT_HASH`: Git commit hash
- `BUILD_DATE`: Build timestamp
- `BACKEND_URL`: Backend API URL

## Local Development

For local development, you don't need to run these workflows. The application works with default values:

- **Backend**: Uses `https://localhost:8443` as frontend URL (from `application.properties`)
- **Frontend**: Uses URL from `front-end/src/config.js`

## Troubleshooting

### Workflow fails with "RAILWAY_TOKEN is invalid"

**Solution**: Generate a new Railway token and update the GitHub Secret

1. Go to Railway → Account Settings → Tokens
2. Create a new token
3. Update `RAILWAY_TOKEN` secret in GitHub

### Environment variables not set

**Solution**: Check GitHub Secrets are configured

1. Go to GitHub repository → Settings → Secrets and variables → Actions
2. Verify `FRONTEND_URL` and `BACKEND_URL` are set
3. Re-run the workflow

### Deployment not triggered

**Solution**: Manually trigger the workflow

1. Go to Actions tab
2. Select "Deploy to Railway"
3. Click "Run workflow"

## Related Documentation

- [Configuração (Secrets e Variáveis)](../../docs/configuracao/README.md)
- [Deploy no Railway](../../docs/deployment/railway.md)
- [CI/CD](../../docs/ci-cd/README.md)
