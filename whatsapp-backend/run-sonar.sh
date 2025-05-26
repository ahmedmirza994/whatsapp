#!/bin/bash

# WhatsApp Backend SonarCloud Analysis Script
echo "Starting SonarCloud analysis for WhatsApp Backend..."

# Check if SonarCloud organization and token are provided
if [ -z "$SONAR_ORGANIZATION" ]; then
    echo "Error: SONAR_ORGANIZATION environment variable is not set"
    echo "Please set it to your SonarCloud organization key"
    exit 1
fi

if [ -z "$SONAR_TOKEN" ]; then
    echo "Error: SONAR_TOKEN environment variable is not set"
    echo "Please set it to your SonarCloud authentication token"
    exit 1
fi

# Set SonarCloud URL
export SONAR_HOST_URL="https://sonarcloud.io"

# Clean and build the project
echo "Cleaning and building the project..."
./gradlew clean build

# Run tests with coverage
echo "Running tests with coverage..."
./gradlew test jacocoTestReport

# Run SonarCloud analysis
echo "Running SonarCloud analysis..."
./gradlew sonarqube \
    -Dsonar.host.url="$SONAR_HOST_URL" \
    -Dsonar.organization="$SONAR_ORGANIZATION" \
    -Dsonar.login="$SONAR_TOKEN"

echo "SonarCloud analysis completed!"
echo "Check your SonarCloud dashboard at: https://sonarcloud.io/organizations/$SONAR_ORGANIZATION"
