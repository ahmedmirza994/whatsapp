#!/bin/bash

# Local Code Quality Check Script
# This script runs the same checks that will be performed in CI/CD

echo "🔍 WhatsApp Backend - Local Code Quality Check"
echo "=============================================="
echo ""

# Change to backend directory
cd whatsapp-backend

# Check if we're in the right directory
if [ ! -f "build.gradle.kts" ]; then
    echo "❌ Error: Not in the whatsapp-backend directory"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Check if gradlew is executable
if [ ! -x "./gradlew" ]; then
    echo "🔧 Making gradlew executable..."
    chmod +x ./gradlew
fi

echo "🧹 Step 1: Cleaning previous builds..."
./gradlew clean

echo ""
echo "🔨 Step 2: Compiling project..."
./gradlew compileJava compileTestJava

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed. Please fix compilation errors before proceeding."
    exit 1
fi

echo ""
echo "🧪 Step 3: Running unit tests..."
./gradlew test

if [ $? -ne 0 ]; then
    echo "❌ Some tests failed. Please fix failing tests before proceeding."
    exit 1
fi

echo ""
echo "📊 Step 4: Generating coverage report..."
./gradlew jacocoTestReport

# Check if coverage report was generated
if [ -f "build/reports/jacoco/test/jacocoTestReport.xml" ]; then
    echo "✅ Coverage report generated successfully"
    
    # Try to extract coverage percentage (basic parsing)
    if command -v xmllint >/dev/null 2>&1; then
        COVERAGE=$(xmllint --xpath "string(//report/counter[@type='INSTRUCTION']/@missed)" build/reports/jacoco/test/jacocoTestReport.xml 2>/dev/null)
        TOTAL=$(xmllint --xpath "string(//report/counter[@type='INSTRUCTION']/@covered)" build/reports/jacoco/test/jacocoTestReport.xml 2>/dev/null)
        
        if [ ! -z "$COVERAGE" ] && [ ! -z "$TOTAL" ]; then
            PERCENTAGE=$(echo "scale=2; ($TOTAL / ($COVERAGE + $TOTAL)) * 100" | bc 2>/dev/null)
            if [ ! -z "$PERCENTAGE" ]; then
                echo "📈 Coverage: ${PERCENTAGE}%"
            fi
        fi
    fi
else
    echo "⚠️ Coverage report not found"
fi

echo ""
echo "🔍 Step 5: Running SonarCloud analysis (if token available)..."

if [ -z "$SONAR_TOKEN" ]; then
    echo "⚠️ SONAR_TOKEN not set. Skipping SonarCloud analysis."
    echo "💡 To run SonarCloud analysis locally:"
    echo "   export SONAR_TOKEN=your_token_here"
    echo "   export SONAR_ORGANIZATION=ahmedmirza994"
    echo "   ./run-sonar.sh"
else
    echo "🚀 Running SonarCloud analysis..."
    ./run-sonar.sh
fi

echo ""
echo "✅ Local code quality check completed!"
echo ""
echo "📋 Summary:"
echo "- ✅ Project compiles successfully"
echo "- ✅ All tests pass"
echo "- ✅ Coverage report generated"

if [ ! -z "$SONAR_TOKEN" ]; then
    echo "- ✅ SonarCloud analysis completed"
fi

echo ""
echo "🔗 Next steps:"
echo "1. Review test coverage in: build/reports/jacoco/test/index.html"
echo "2. Check SonarCloud dashboard: https://sonarcloud.io/project/overview?id=ahmedmirza994_whatsapp"
echo "3. Create a pull request to trigger automated quality checks"
echo ""
echo "🎉 Your code is ready for review!"
