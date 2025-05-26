#!/bin/bash

echo "🚀 Formatting entire WhatsApp Clone project..."
echo "================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Track success
JAVA_SUCCESS=false
ANGULAR_SUCCESS=false

echo -e "${BLUE}📋 Code Formatting Summary:${NC}"
echo "• Java Backend: Spotless + Google Java Format"
echo "• Angular Frontend: Prettier + ESLint"
echo "• Line Length: 200 characters for both projects"
echo ""

# Format Java backend
echo -e "${YELLOW}🔧 Step 1: Formatting Java Backend...${NC}"
./format-java.sh
if [ $? -eq 0 ]; then
    JAVA_SUCCESS=true
    echo -e "${GREEN}✅ Java formatting completed successfully!${NC}"
else
    echo -e "${RED}❌ Java formatting failed!${NC}"
fi

echo ""

# Format Angular frontend  
echo -e "${YELLOW}🔧 Step 2: Formatting Angular Frontend...${NC}"
./format-angular.sh
if [ $? -eq 0 ]; then
    ANGULAR_SUCCESS=true
    echo -e "${GREEN}✅ Angular formatting completed successfully!${NC}"
else
    echo -e "${RED}❌ Angular formatting failed!${NC}"
fi

echo ""
echo "================================================"

# Final summary
if [ "$JAVA_SUCCESS" = true ] && [ "$ANGULAR_SUCCESS" = true ]; then
    echo -e "${GREEN}🎉 All code formatted successfully!${NC}"
    echo -e "${BLUE}💡 Tips:${NC}"
    echo "• Run this script before committing your changes"
    echo "• Configure your IDE to format on save"
    echo "• The CI/CD pipeline will check formatting automatically"
    echo ""
    echo -e "${BLUE}📊 Next steps:${NC}"
    echo "• Check git status: git status"
    echo "• Review changes: git diff"
    echo "• Commit formatted code: git add . && git commit -m 'style: apply code formatting'"
    exit 0
else
    echo -e "${RED}❌ Some formatting tasks failed!${NC}"
    echo "Please check the error messages above and fix any issues."
    exit 1
fi
