# Backend Static Resources - DEPRECATED

⚠️ **IMPORTANT**: This directory is **DEPRECATED** and should **NOT** be used for development.

## Why is this directory here?

Historically, this directory was used to serve the frontend directly from the Spring Boot backend. However, the project has evolved to use a **separate frontend service** architecture.

## Current Architecture

- **Production**: Frontend and backend are **separate services**
  - Frontend: Served by static file server from `front-end/` directory
  - Backend: Spring Boot API server (does NOT serve static files)

- **Development**: You should run frontend separately
  - Use Live Server, Python HTTP Server, or `serve` to run the `front-end/` directory
  - Backend runs on port 8443 with API endpoints only

## Source of Truth

The **`front-end/src/`** directory is the **ONLY source of truth** for frontend code.

❌ **DO NOT** edit files in `back-end/src/main/resources/static/`
✅ **ALWAYS** edit files in `front-end/src/`

## Migration Status

Most files in this directory are **outdated** and do **NOT** match the current frontend:

- ✅ **Up to date**: client-login.html, client-register.html, client-preferences-setup.html
- ❌ **Outdated**: All other files

## Recommendation

This directory should be removed in a future cleanup, but is kept for now to avoid breaking any legacy local development setups.

If you're using this directory for local development, please switch to serving the `front-end/` directory separately.

## How to Run Frontend Locally

```bash
cd front-end/src

# Option 1: Using Python
python3 -m http.server 5500

# Option 2: Using serve (npm)
npm install -g serve
serve -s . -l 5500

# Option 3: Using VS Code Live Server extension
# Just right-click on index.html and select "Open with Live Server"
```

The frontend will be available at `http://localhost:5500` and will connect to the backend at `https://localhost:8443`.
