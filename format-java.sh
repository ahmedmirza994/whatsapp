#!/bin/bash

echo "🎨 Formatting Java code with Spotless..."
cd whatsapp-backend

# Check if we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: Not in the whatsapp-backend directory"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Make gradlew executable if needed
if [ ! -x "./gradlew" ]; then
    echo "🔧 Making gradlew executable..."
    chmod +x ./gradlew
fi

echo "🔍 Checking Java code formatting..."

# Check if formatting is needed
if ./gradlew spotlessCheck --quiet; then
    echo "✅ Java code is already properly formatted!"
else
    echo "🔧 Applying Java formatting..."
    ./gradlew spotlessApply
    if [ $? -eq 0 ]; then
        echo "✅ Java code formatted successfully!"
        echo "📝 Changes applied with 200-character line length"
    else
        echo "❌ Java formatting failed!"
        exit 1
    fi
fi

cd ..
echo "🎉 Java formatting completed!"
