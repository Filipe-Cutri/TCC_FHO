#!/bin/bash
# check-versions.sh - Check deployed versions of frontend and backend

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default URLs
BACKEND_URL="${1:-https://tccfho-production.up.railway.app}"
FRONTEND_URL="${2:-https://tccfho-production-baff.up.railway.app}"

# Function to print section header
print_header() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# Function to check if jq is installed
check_jq() {
    if ! command -v jq &> /dev/null; then
        echo -e "${YELLOW}⚠️  jq is not installed. Installing for better JSON formatting...${NC}"
        if command -v apt-get &> /dev/null; then
            sudo apt-get update && sudo apt-get install -y jq
        elif command -v brew &> /dev/null; then
            brew install jq
        else
            echo -e "${YELLOW}Please install jq manually for better output formatting${NC}"
            return 1
        fi
    fi
    return 0
}

# Main script
echo -e "${GREEN}"
echo "╔════════════════════════════════════════╗"
echo "║   🔍 Slotfy Version Checker           ║"
echo "╔════════════════════════════════════════╝"
echo -e "${NC}"

check_jq
HAS_JQ=$?

# Check Backend
print_header "📦 BACKEND - $BACKEND_URL"

BACKEND_HEALTH=$(curl -s -w "\n%{http_code}" "$BACKEND_URL/api/health" 2>&1)
BACKEND_HTTP_CODE=$(echo "$BACKEND_HEALTH" | tail -n1)
BACKEND_BODY=$(echo "$BACKEND_HEALTH" | sed '$d')

if [ "$BACKEND_HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✅ Status: HTTP $BACKEND_HTTP_CODE (OK)${NC}"
    echo ""
    if [ $HAS_JQ -eq 0 ]; then
        echo "$BACKEND_BODY" | jq '.'
    else
        echo "$BACKEND_BODY"
    fi
    
    # Extract key info
    if [ $HAS_JQ -eq 0 ]; then
        VERSION=$(echo "$BACKEND_BODY" | jq -r '.version // "unknown"')
        COMMIT=$(echo "$BACKEND_BODY" | jq -r '.commit // "unknown"')
        UPTIME=$(echo "$BACKEND_BODY" | jq -r '.uptime // "unknown"')
        ENV=$(echo "$BACKEND_BODY" | jq -r '.environment // "unknown"')
        
        echo ""
        echo -e "${GREEN}📊 Summary:${NC}"
        echo -e "  Version:     ${BLUE}$VERSION${NC}"
        echo -e "  Commit:      ${BLUE}$COMMIT${NC}"
        echo -e "  Environment: ${BLUE}$ENV${NC}"
        echo -e "  Uptime:      ${BLUE}$UPTIME ms${NC}"
    fi
elif [ "$BACKEND_HTTP_CODE" = "500" ]; then
    echo -e "${RED}❌ Status: HTTP $BACKEND_HTTP_CODE (Internal Server Error)${NC}"
    echo ""
    echo -e "${RED}Error Details:${NC}"
    if [ $HAS_JQ -eq 0 ]; then
        echo "$BACKEND_BODY" | jq '.'
    else
        echo "$BACKEND_BODY"
    fi
else
    echo -e "${RED}❌ Status: HTTP $BACKEND_HTTP_CODE (Service Unavailable)${NC}"
    echo -e "${YELLOW}Response:${NC}"
    echo "$BACKEND_BODY"
fi

# Check Frontend
print_header "🎨 FRONTEND - $FRONTEND_URL"

FRONTEND_ROOT=$(curl -s -w "\n%{http_code}" "$FRONTEND_URL/" 2>&1)
FRONTEND_ROOT_CODE=$(echo "$FRONTEND_ROOT" | tail -n1)

if [ "$FRONTEND_ROOT_CODE" = "200" ]; then
    echo -e "${GREEN}✅ Root Status: HTTP $FRONTEND_ROOT_CODE (OK)${NC}"
else
    echo -e "${RED}❌ Root Status: HTTP $FRONTEND_ROOT_CODE${NC}"
fi

echo ""
echo "Checking version info..."

FRONTEND_VERSION=$(curl -s -w "\n%{http_code}" "$FRONTEND_URL/version.json" 2>&1)
FRONTEND_VERSION_CODE=$(echo "$FRONTEND_VERSION" | tail -n1)
FRONTEND_VERSION_BODY=$(echo "$FRONTEND_VERSION" | sed '$d')

if [ "$FRONTEND_VERSION_CODE" = "200" ]; then
    echo -e "${GREEN}✅ Version endpoint: HTTP $FRONTEND_VERSION_CODE (OK)${NC}"
    echo ""
    if [ $HAS_JQ -eq 0 ]; then
        echo "$FRONTEND_VERSION_BODY" | jq '.'
    else
        echo "$FRONTEND_VERSION_BODY"
    fi
    
    # Extract key info
    if [ $HAS_JQ -eq 0 ]; then
        VERSION=$(echo "$FRONTEND_VERSION_BODY" | jq -r '.version // "unknown"')
        COMMIT=$(echo "$FRONTEND_VERSION_BODY" | jq -r '.commit // "unknown"')
        BUILD_DATE=$(echo "$FRONTEND_VERSION_BODY" | jq -r '.buildDate // "unknown"')
        ENV=$(echo "$FRONTEND_VERSION_BODY" | jq -r '.environment // "unknown"')
        
        echo ""
        echo -e "${GREEN}📊 Summary:${NC}"
        echo -e "  Version:     ${BLUE}$VERSION${NC}"
        echo -e "  Commit:      ${BLUE}$COMMIT${NC}"
        echo -e "  Environment: ${BLUE}$ENV${NC}"
        echo -e "  Build Date:  ${BLUE}$BUILD_DATE${NC}"
    fi
else
    echo -e "${RED}❌ Version endpoint: HTTP $FRONTEND_VERSION_CODE${NC}"
    echo -e "${YELLOW}Response:${NC}"
    echo "$FRONTEND_VERSION_BODY"
fi

# Local comparison
print_header "💻 LOCAL REPOSITORY"

if [ -d ".git" ]; then
    LOCAL_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    LOCAL_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "no tags")
    LOCAL_BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
    
    echo -e "${GREEN}Repository Info:${NC}"
    echo -e "  Branch: ${BLUE}$LOCAL_BRANCH${NC}"
    echo -e "  Tag:    ${BLUE}$LOCAL_TAG${NC}"
    echo -e "  Commit: ${BLUE}$LOCAL_COMMIT${NC}"
else
    echo -e "${YELLOW}⚠️  Not a git repository${NC}"
fi

# Final Summary
print_header "📋 SUMMARY"

echo -e "${GREEN}Deployment Status:${NC}"
if [ "$BACKEND_HTTP_CODE" = "200" ]; then
    echo -e "  Backend:  ${GREEN}✅ Healthy${NC}"
else
    echo -e "  Backend:  ${RED}❌ Unhealthy (HTTP $BACKEND_HTTP_CODE)${NC}"
fi

if [ "$FRONTEND_ROOT_CODE" = "200" ]; then
    echo -e "  Frontend: ${GREEN}✅ Healthy${NC}"
else
    echo -e "  Frontend: ${RED}❌ Unhealthy (HTTP $FRONTEND_ROOT_CODE)${NC}"
fi

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}✨ Check complete!${NC}"
echo ""
