# SonarCloud Integration for WhatsApp Backend

This document explains how to set up and use SonarCloud for code quality analysis in the WhatsApp Backend project.

## Prerequisites

-   Java 21
-   Gradle
-   GitHub, Bitbucket, or Azure DevOps account
-   SonarCloud account

## SonarCloud Setup

### Step 1: Create SonarCloud Account

1. **Visit SonarCloud:**

    - Go to https://sonarcloud.io
    - Sign up/Login with your GitHub, Bitbucket, or Azure DevOps account

2. **Import Your Repository:**

    - Click "+" → "Analyze new project"
    - Select your Git provider and authorize SonarCloud
    - Choose your WhatsApp project repository

3. **Configure Organization:**
    - Create or select an organization
    - Note your organization key (you'll need this)

### Step 2: Get Authentication Token

1. **Generate Token:**
    - Go to your SonarCloud account → Security tab
    - Click "Generate Tokens"
    - Create a token with a descriptive name
    - Copy the token (you won't see it again)

### Step 3: Configure Project

1. **Update Configuration Files:**

    In `sonar-project.properties`, replace:

    ```properties
    sonar.organization=YOUR_SONARCLOUD_ORGANIZATION_KEY
    ```

    With your actual organization key.

2. **Set Environment Variables:**

    ```bash
    export SONAR_ORGANIZATION=your_organization_key
    export SONAR_TOKEN=your_generated_token
    ```

    To make these permanent, add them to your shell profile:

    ```bash
    echo 'export SONAR_ORGANIZATION=your_organization_key' >> ~/.zshrc
    echo 'export SONAR_TOKEN=your_generated_token' >> ~/.zshrc
    source ~/.zshrc
    ```

### Step 4: Run Analysis

Run the analysis script:

```bash
./run-sonar.sh
```

Or run manually:

```bash
# Clean and build
./gradlew clean build

# Run tests with coverage
./gradlew test jacocoTestReport

# Run SonarCloud analysis
./gradlew sonarqube \
    -Dsonar.host.url=https://sonarcloud.io \
    -Dsonar.organization=$SONAR_ORGANIZATION \
    -Dsonar.login=$SONAR_TOKEN
```

## CI/CD Integration

### GitHub Actions

Create `.github/workflows/sonarcloud.yml`:

```yaml
name: SonarCloud Analysis

on:
    push:
        branches: [main, develop]
    pull_request:
        branches: [main]

jobs:
    sonarcloud:
        runs-on: ubuntu-latest

        steps:
            - uses: actions/checkout@v4
              with:
                  fetch-depth: 0 # Shallow clones should be disabled for better analysis

            - name: Set up JDK 21
              uses: actions/setup-java@v4
              with:
                  java-version: "21"
                  distribution: "temurin"

            - name: Cache Gradle packages
              uses: actions/cache@v4
              with:
                  path: ~/.gradle/caches
                  key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle.kts') }}
                  restore-keys: ${{ runner.os }}-gradle

            - name: Cache SonarCloud packages
              uses: actions/cache@v4
              with:
                  path: ~/.sonar/cache
                  key: ${{ runner.os }}-sonar
                  restore-keys: ${{ runner.os }}-sonar

            - name: Make gradlew executable
              run: chmod +x ./gradlew

            - name: Run tests
              run: ./gradlew test jacocoTestReport

            - name: SonarCloud Scan
              env:
                  GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
                  SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
                  SONAR_ORGANIZATION: ${{ secrets.SONAR_ORGANIZATION }}
              run: ./gradlew sonarqube
```

### Required GitHub Secrets

Add these secrets to your GitHub repository:

-   `SONAR_TOKEN`: Your SonarCloud authentication token
-   `SONAR_ORGANIZATION`: Your SonarCloud organization key

### GitLab CI

Create `.gitlab-ci.yml`:

```yaml
sonarcloud-check:
    image: gradle:8.5-jdk21
    variables:
        SONAR_USER_HOME: "${CI_PROJECT_DIR}/.sonar"
        GIT_DEPTH: "0"
    cache:
        key: "${CI_JOB_NAME}"
        paths:
            - .sonar/cache
    script:
        - ./gradlew test jacocoTestReport sonarqube
    only:
        - merge_requests
        - main
        - develop
```

### Azure DevOps

Add this to your `azure-pipelines.yml`:

```yaml
- task: SonarCloudPrepare@1
  inputs:
      SonarCloud: "SonarCloud"
      organization: "$(SONAR_ORGANIZATION)"
      scannerMode: "Other"

- task: Gradle@2
  inputs:
      workingDirectory: ""
      gradleWrapperFile: "gradlew"
      gradleOptions: "-Xmx3072m"
      tasks: "test jacocoTestReport sonarqube"

- task: SonarCloudPublish@1
```

## Configuration Files

-   `build.gradle.kts` - Contains SonarQube plugin and configuration
-   `sonar-project.properties` - SonarCloud project configuration
-   `run-sonar.sh` - Analysis execution script

## Quality Gates

The project is configured with quality gates that will:

-   Fail the build if coverage is below threshold
-   Check for code smells, bugs, and vulnerabilities
-   Enforce coding standards and best practices

## Project Dashboard

After running the analysis, you can view results at:
https://sonarcloud.io/organizations/YOUR_ORGANIZATION/projects

## Troubleshooting

1. **Permission denied for run-sonar.sh:**

    ```bash
    chmod +x run-sonar.sh
    ```

2. **Authentication failed:**

    - Verify your SONAR_TOKEN is correct
    - Check that the token hasn't expired
    - Ensure you have proper permissions on the project

3. **Organization not found:**

    - Verify SONAR_ORGANIZATION matches your SonarCloud organization key
    - Check that the project is imported in the correct organization

4. **Coverage reports not found:**

    ```bash
    ./gradlew clean test jacocoTestReport
    ```

5. **Java version issues:**

    - Ensure Java 21 is installed and JAVA_HOME is set correctly

6. **Git repository issues:**
    - Ensure your repository is connected to SonarCloud
    - Check that you have the necessary permissions

## Benefits of SonarCloud

-   **Free for public repositories**
-   **Automatic pull request analysis**
-   **Integration with major Git providers**
-   **No infrastructure management required**
-   **Advanced security analysis**
-   **Quality gate integration with CI/CD**

## Next Steps

1. Set up your SonarCloud account and organization
2. Configure environment variables
3. Run your first analysis
4. Set up CI/CD integration
5. Configure quality gates according to your project needs
