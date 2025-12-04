#!/bin/bash
# This script injects the backend URL and version info into the frontend configuration
# It should be run during Railway deployment

# Get the backend URL from environment variable
BACKEND_URL=${BACKEND_URL:-""}

# Get version information from environment variables
APP_VERSION=${APP_VERSION:-"unknown"}
COMMIT_HASH=${COMMIT_HASH:-"unknown"}
BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Determine the correct path for config.js
# In Railway, the working directory is the root of the service (front-end/)
CONFIG_PATH="src/config.js"
VERSION_PATH="src/version.json"

# Create or update the config.js file with the backend URL
cat > "$CONFIG_PATH" << EOF
/**
 * Runtime Configuration for Slotfy Frontend
 * This file is auto-generated during deployment
 */

// Backend URL for API calls
window.BACKEND_URL = '${BACKEND_URL}';

// Environment configuration
window.APP_CONFIG = {
    environment: '${RAILWAY_ENVIRONMENT:-production}',
    apiVersion: 'v1'
};
EOF

# Create version.json file
cat > "$VERSION_PATH" << EOF
{
  "service": "frontend",
  "version": "${APP_VERSION}",
  "commit": "${COMMIT_HASH}",
  "buildDate": "${BUILD_DATE}",
  "environment": "${RAILWAY_ENVIRONMENT:-production}"
}
EOF

echo "Frontend configuration updated with BACKEND_URL: ${BACKEND_URL}"
echo "Config file written to: $CONFIG_PATH"
echo "Version file written to: $VERSION_PATH with version ${APP_VERSION}, commit ${COMMIT_HASH}"