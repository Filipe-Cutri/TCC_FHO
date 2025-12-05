# Quick Start - Railway Deployment Fixes

## 🚀 Immediate Actions After Merging

### Step 1: Verify RAILWAY_TOKEN is Set
```bash
# Go to GitHub Repository Settings
# Navigate to: Settings → Secrets and variables → Actions
# Add a new secret:
#   Name: RAILWAY_TOKEN
#   Value: <your-railway-api-token>
```

**To get Railway token:**
1. Go to Railway dashboard
2. Click on your profile (bottom left)
3. Account Settings → Tokens
4. Create new token → Copy it

### Step 2: Check Railway Service Names

The deployment workflow expects these service names:
- Backend: `backend`
- Frontend: `frontend`

**If your services have different names:**
1. Edit `.github/workflows/deploy.yml`
2. Replace `--service backend` with your actual backend service name
3. Replace `--service frontend` with your actual frontend service name

### Step 3: Trigger First Deployment

**Option A: Merge to Main (Automatic)**
```bash
# Just merge this PR - it will automatically:
# 1. Create a version tag (via release.yml)
# 2. Run deploy workflow
# 3. Set environment variables in Railway
# 4. Trigger redeployment
```

**Option B: Manual Trigger**
1. Go to GitHub Actions
2. Click "Deploy to Railway" workflow
3. Click "Run workflow"
4. Select branch: `main`
5. Click green "Run workflow" button

### Step 4: Verify Deployment

```bash
# Clone the repo and run the check script
git clone <repo-url>
cd TCC_FHO
chmod +x check-versions.sh
./check-versions.sh
```

Expected output:
```
✅ Backend:  HTTP 200 (OK)
   Version: v0.0.37
   Commit:  b262350

✅ Frontend: HTTP 200 (OK)
   Version: v0.0.37
   Commit:  b262350
```

---

## 🔍 Quick Health Checks

### Backend Health
```bash
curl https://tccfho-production.up.railway.app/api/health
```

### Frontend Version
```bash
curl https://tccfho-production-baff.up.railway.app/version.json
```

### Browser Console
1. Open: https://tccfho-production-baff.up.railway.app
2. Press F12
3. Look for "🚀 Slotfy Frontend" in console
4. Type: `window.SLOTFY_VERSION`

---

## ⚠️ Troubleshooting

### Workflow Fails with "service not found"

**Problem:** Railway service names don't match workflow

**Fix:**
```bash
# Check your actual service names in Railway dashboard
# Then update .github/workflows/deploy.yml:
# Line 50, 53, 56: Change --service backend
# Line 69, 72, 75: Change --service frontend
```

### Backend Still Returns 500

**Possible causes:**
1. Environment variables not set yet
2. Database connection issue

**Check:**
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login
railway login

# Check variables
railway variables --service backend

# Should see: APP_VERSION, COMMIT_HASH, BUILD_DATE
# If not, run the deploy workflow again
```

### Version Shows "unknown"

**Cause:** Environment variables not set in Railway

**Fix:**
```bash
# Manual set (temporary)
railway variables set APP_VERSION="v0.0.37"
railway variables set COMMIT_HASH="$(git rev-parse --short HEAD)"

# Or trigger deploy workflow to set automatically
```

---

## 📖 Full Documentation

- **Complete Guide:** `RAILWAY_DEPLOYMENT_GUIDE.md`
- **Implementation Details:** `IMPLEMENTATION_SUMMARY.md`
- **Version Checker:** `./check-versions.sh`

---

## ✅ Checklist

Before closing this PR, verify:
- [ ] RAILWAY_TOKEN secret is set in GitHub
- [ ] Railway service names match workflow (or workflow updated)
- [ ] Deployment workflow ran successfully
- [ ] Backend /health returns 200 with version info
- [ ] Frontend /version.json returns 200 with version info
- [ ] Version matches latest git tag
- [ ] No errors in Railway logs

---

## 🆘 Still Having Issues?

1. Check Railway logs:
   ```bash
   railway logs --service backend
   railway logs --service frontend
   ```

2. Review workflow run in GitHub Actions

3. Read `RAILWAY_DEPLOYMENT_GUIDE.md` for detailed troubleshooting

4. Verify all environment variables are set:
   ```bash
   railway variables --service backend
   railway variables --service frontend
   ```

---

## 🎯 Success Criteria

You'll know it's working when:
- ✅ `./check-versions.sh` shows all green checkmarks
- ✅ Backend `/api/health` returns version and commit
- ✅ Frontend console shows version info
- ✅ Both versions match the latest git tag
- ✅ No errors in Railway logs

---

That's it! You're ready to go! 🚀
