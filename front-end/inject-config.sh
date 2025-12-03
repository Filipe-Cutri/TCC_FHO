#!/bin/bash
# This script injects the backend URL into the frontend configuration
# It should be run during Railway deployment

# Get the backend URL from environment variable
BACKEND_URL=${BACKEND_URL:-""}

# Determine the correct path for config.js
# In Railway, the working directory is the root of the service (front-end/)
CONFIG_PATH="src/config.js"

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

echo "Frontend configuration updated with BACKEND_URL: ${BACKEND_URL}"
echo "Config file written to: $CONFIG_PATH"