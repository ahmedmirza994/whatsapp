# GitHub Actions for WhatsApp Project

This directory contains GitHub Actions workflows for automated testing, code quality analysis, and reporting.

## 🚀 Workflows

### 1. SonarCloud Code Quality Analysis (`sonarcloud.yml`)

**Triggers:**

-   Push to `main` branch (backend changes only)
-   Pull requests to `main` branch (backend changes only)

**Features:**

-   ✅ Comprehensive code quality analysis with SonarCloud
-   📊 Automatic test coverage reporting
-   🔒 Security vulnerability scanning
-   🎯 Quality gate enforcement
-   💬 PR comments with analysis results
-   📈 Code coverage badges and reports
-   🔗 Direct links to SonarCloud dashboard

**What it provides in GitHub:**

-   **PR Comments**: Code coverage percentages and quality metrics
-   **Check Status**: Pass/fail status for quality gates
-   **Job Summary**: Detailed analysis report with links
-   **Artifacts**: Test results and coverage reports

### 2. Backend Build & Test (`build-test.yml`)

**Triggers:**

-   Push to `main` or `develop` branches (backend changes only)
-   Pull requests to `main` branch (backend changes only)

**Features:**

-   🔨 Project compilation and build verification
-   🧪 Unit test execution with detailed reporting
-   📊 Test coverage generation and reporting
-   📁 Artifact upload for test results and coverage
-   💬 PR comments with coverage details

## 📋 Required GitHub Secrets

Add these secrets to your GitHub repository settings:

1. **`SONAR_TOKEN`**

    - Go to [SonarCloud Security](https://sonarcloud.io/account/security)
    - Generate a new token
    - Add it as a repository secret

2. **`GITHUB_TOKEN`**
    - Automatically provided by GitHub Actions
    - No manual setup required

## 🎯 What You'll See in GitHub

### Pull Request Comments

-   **Code Coverage Report**: Detailed coverage percentages for overall project and changed files
-   **SonarCloud Analysis**: Quality gate status, code smells, bugs, and security issues
-   **Test Results**: Number of passed/failed tests

### Check Status

-   ✅ **SonarCloud Quality Gate**: Pass/fail status
-   ✅ **Build Status**: Compilation and test execution status
-   ✅ **Coverage Threshold**: Meets minimum coverage requirements

### Job Summaries

-   📊 Test execution summary with counts
-   📈 Coverage report links
-   🔗 Direct links to SonarCloud dashboard
-   🎯 Quality gate status and recommendations

### Artifacts

-   📁 **Test Results**: JUnit XML reports
-   📁 **Coverage Reports**: JaCoCo XML and HTML reports
-   🔄 **Retention**: 7 days for all artifacts

## 🔧 Configuration

### SonarCloud Settings

The workflows are configured for the SonarCloud organization: `ahmedmirza994`

### Coverage Thresholds

-   **Overall Project**: 70% minimum coverage
-   **Changed Files**: 80% minimum coverage

### Path Filtering

Workflows only trigger for changes in the `whatsapp-backend/` directory to optimize CI/CD performance.

## 📊 Dashboard Links

After running the workflows, you can access:

-   [📊 SonarCloud Project Dashboard](https://sonarcloud.io/project/overview?id=whatsapp-backend)
-   [📋 Coverage Details](https://sonarcloud.io/component_measures?id=whatsapp-backend&metric=coverage)
-   [🔍 Security Analysis](https://sonarcloud.io/project/security_hotspots?id=whatsapp-backend)
-   [🎯 Quality Gate](https://sonarcloud.io/project/quality_gate?id=whatsapp-backend)

## 🛠️ Local Testing

To test the same analysis locally:

```bash
cd whatsapp-backend

# Run tests and generate coverage
./gradlew clean test jacocoTestReport

# Run SonarCloud analysis (requires SONAR_TOKEN)
export SONAR_TOKEN=your_token_here
./run-sonar.sh
```

## 🎨 Customization

### Adjusting Coverage Thresholds

Edit the `min-coverage-overall` and `min-coverage-changed-files` values in the workflow files.

### Adding New Quality Checks

Modify the SonarCloud configuration in `sonar-project.properties` to add custom rules or exclusions.

### Changing Trigger Conditions

Update the `on` section in workflow files to modify when the actions run.

## 🔍 Troubleshooting

### Common Issues

1. **SonarCloud Authentication Failed**

    - Verify `SONAR_TOKEN` secret is correctly set
    - Ensure token has proper permissions

2. **Coverage Reports Not Found**

    - Check that tests are running successfully
    - Verify JaCoCo plugin configuration in `build.gradle.kts`

3. **Quality Gate Failures**
    - Review SonarCloud dashboard for specific issues
    - Check code smells, bugs, and security hotspots

### Getting Help

-   📖 [SonarCloud Documentation](https://docs.sonarcloud.io/)
-   🔧 [GitHub Actions Documentation](https://docs.github.com/en/actions)
-   💬 Check the Actions tab in your GitHub repository for detailed logs
