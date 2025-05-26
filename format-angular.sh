#!/bin/bash

echo "🎨 Formatting Angular code with Prettier + ESLint..."
cd whatsapp-web

# Check if we're in the right directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: Not in the whatsapp-web directory"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

echo "🔍 Checking Angular code formatting..."

# Check Prettier formatting
if npm run format:check --silent; then
    echo "✅ Angular code is already properly formatted!"
    NEEDS_FORMAT=false
else
    echo "🔧 Applying Prettier formatting..."
    npm run format
    if [ $? -eq 0 ]; then
        echo "✅ Prettier formatting applied successfully!"
        echo "📝 Code formatted with tabs (4 spaces) and 200-character line length"
        echo "📝 HTML tags with 3+ attributes formatted on multiple lines"
        NEEDS_FORMAT=true
    else
        echo "❌ Prettier formatting failed!"
        exit 1
    fi
fi

echo "🔍 Running ESLint checks..."

# Check and fix ESLint issues
if npm run lint --silent 2>/dev/null; then
    echo "✅ ESLint passed!"
else
    echo "🔧 Fixing ESLint issues..."
    npm run lint:fix
    if [ $? -eq 0 ]; then
        echo "✅ ESLint issues fixed!"
    else
        echo "⚠️  Some ESLint issues require manual fixing"
        echo "📋 Run 'npm run lint' to see remaining issues"
    fi
fi

cd ..

if [ "$NEEDS_FORMAT" = true ]; then
    echo "🎉 Angular formatting completed with changes!"
else
    echo "🎉 Angular formatting completed - no changes needed!"
fi
