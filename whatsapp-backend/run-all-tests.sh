#!/bin/bash

# WhatsApp Backend - Run All Tests Script
# This script runs both unit tests and integration tests, then generates a combined coverage report

set -e

echo "🧪 Running WhatsApp Backend Tests"
echo "=================================="

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Compile project
echo "🔨 Compiling project..."
./gradlew compileJava compileTestJava compileIntegrationTestJava

# Run unit tests
echo "🧪 Running unit tests..."
./gradlew test --continue

# Run integration tests
echo "🧪 Running integration tests..."
./gradlew integrationTest --continue

# Generate combined coverage report
echo "📊 Generating combined coverage report..."
./gradlew jacocoTestReport

# Display results
echo ""
echo "📋 Test Results Summary"
echo "======================="

if [ -d "build/test-results/test" ]; then
    UNIT_TESTS=$(find build/test-results/test -name "*.xml" -exec grep -o "tests=\"[0-9]*\"" {} \; | grep -o "[0-9]*" | awk '{sum += $1} END {print sum}')
    UNIT_FAILURES=$(find build/test-results/test -name "*.xml" -exec grep -o "failures=\"[0-9]*\"" {} \; | grep -o "[0-9]*" | awk '{sum += $1} END {print sum}')
    echo "Unit Tests: ${UNIT_TESTS:-0} total, ${UNIT_FAILURES:-0} failed"
fi

if [ -d "build/test-results/integrationTest" ]; then
    INTEGRATION_TESTS=$(find build/test-results/integrationTest -name "*.xml" -exec grep -o "tests=\"[0-9]*\"" {} \; | grep -o "[0-9]*" | awk '{sum += $1} END {print sum}')
    INTEGRATION_FAILURES=$(find build/test-results/integrationTest -name "*.xml" -exec grep -o "failures=\"[0-9]*\"" {} \; | grep -o "[0-9]*" | awk '{sum += $1} END {print sum}')
    echo "Integration Tests: ${INTEGRATION_TESTS:-0} total, ${INTEGRATION_FAILURES:-0} failed"
fi

echo ""
echo "📊 Coverage Report"
echo "=================="

if [ -f "build/reports/jacoco/test/jacocoTestReport.xml" ]; then
    echo "✅ Combined coverage report generated successfully"
    echo "📁 Location: build/reports/jacoco/test/jacocoTestReport.xml"
    echo "🌐 HTML Report: build/reports/jacoco/test/html/index.html"

    # Try to extract overall coverage percentage
    if command -v grep &> /dev/null && command -v awk &> /dev/null; then
        COVERAGE=$(grep -o 'type="INSTRUCTION".*missed="[0-9]*".*covered="[0-9]*"' build/reports/jacoco/test/jacocoTestReport.xml | head -1 | awk -F'"' '{
            missed=$4; covered=$6;
            if(missed+covered > 0)
                printf "%.1f", (covered/(missed+covered))*100
        }')
        if [ ! -z "$COVERAGE" ]; then
            echo "📈 Overall Coverage: $COVERAGE%"
        fi
    fi
else
    echo "❌ Coverage report not found"
fi

echo ""
echo "🎉 Test execution completed!"
echo ""
echo "💡 To run SonarQube analysis:"
echo "   ./run-sonar.sh"
echo ""
echo "💡 To view coverage report:"
echo "   open build/reports/jacoco/test/html/index.html"
