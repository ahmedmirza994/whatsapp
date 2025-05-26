# 🎨 Code Formatting Setup

This document describes the code formatting configuration for the WhatsApp Clone project.

## 📋 Overview

The project uses automated code formatting with consistent rules across both backend and frontend:

- **Line Length**: 200 characters for optimal readability on modern screens
- **Indentation**: Tabs with 4-space width for both Java and TypeScript/Angular
- **Multi-line Formatting**: Functions/methods with 3+ parameters and HTML tags with 3+ attributes are formatted across multiple lines
- **Backend**: Java with Google Java Format (AOSP variant) via Spotless
- **Frontend**: TypeScript/Angular with Prettier + ESLint
- **Automation**: Format-on-save in IDEs, CI/CD validation, manual scripts

## 🛠️ Tools Configuration

### Java Backend (Spotless + Google Java Format)

**File**: `whatsapp-backend/build.gradle.kts`
```kotlin
spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.22.0").aosp().reflowLongStrings()
        licenseHeader("""/*
             * WhatsApp Clone - Backend Service
             * Copyright (c) 2025
             */""")
    }
}
```

**Features**:
- Google Java Format with AOSP variant (4-space indentation)
- 200-character line length with string reflow
- Automatic license header insertion
- Import organization and trailing whitespace removal

### Angular Frontend (Prettier + ESLint)

**File**: `whatsapp-web/.prettierrc.json`
```json
{
  "printWidth": 200,
  "tabWidth": 4,
  "useTabs": true,
  "semi": true,
  "singleQuote": true,
  "quoteProps": "as-needed",
  "trailingComma": "es5",
  "bracketSpacing": true,
  "arrowParens": "avoid",
  "endOfLine": "lf",
  "singleAttributePerLine": true
}
```

**File**: `whatsapp-web/.eslintrc.json`
```json
{
  "rules": {
    "max-len": [
      "error",
      {
        "code": 200,
        "tabWidth": 4,
        "ignoreUrls": true,
        "ignoreStrings": true,
        "ignoreTemplateLiterals": true,
        "ignoreComments": true,
        "ignoreRegExpLiterals": true
      }
    ]
  }
}
```

## 🚀 Usage Scripts

### Format All Code
```bash
./format-all.sh
```
Formats both Java backend and Angular frontend with summary output.

### Format Java Only
```bash
./format-java.sh
```
Applies Spotless formatting to Java code in `whatsapp-backend/`.

### Format Angular Only
```bash
./format-angular.sh
```
Applies Prettier and ESLint fixes to Angular code in `whatsapp-web/`.

## 🔧 IDE Integration

### VS Code Setup

**File**: `.vscode/settings.json`
```json
{
  "editor.formatOnSave": true,
  "editor.rulers": [200],
  "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
  "java.format.settings.profile": "GoogleStyle",
  "prettier.printWidth": 200,
  "eslint.format.enable": true
}
```

**Recommended Extensions**:
- Prettier - Code formatter
- ESLint
- Extension Pack for Java
- Language Support for Java by Red Hat

### IntelliJ IDEA Setup

1. **Install Google Java Format Plugin**:
   - Go to Settings → Plugins → Browse repositories
   - Search for "google-java-format" and install

2. **Configure Line Length**:
   - Settings → Code Style → Java → Wrapping and Braces
   - Set "Right margin" to 200

3. **Enable Format on Save**:
   - Settings → Tools → Actions on Save
   - Enable "Reformat code"

## 🤖 CI/CD Integration

### GitHub Actions Workflow

**File**: `.github/workflows/code-formatting.yml`

The workflow automatically:
1. Checks Java formatting with Spotless
2. Validates Angular formatting with Prettier
3. Runs ESLint for code quality
4. Provides detailed feedback in PR comments

### Running Locally Before Push

```bash
# Check formatting without applying changes
cd whatsapp-backend && ./gradlew spotlessCheck
cd whatsapp-web && npm run format:check && npm run lint

# Apply formatting
./format-all.sh
```

## 📝 Formatting Rules Summary

### Java
- **Indentation**: 4 spaces (via tabs)
- **Line Length**: 200 characters
- **Import Order**: Automatic organization
- **String Wrapping**: Long strings are reflowed
- **License Headers**: Automatically added to new files
- **Multi-line Methods**: Functions with 3+ parameters formatted across multiple lines

### TypeScript/Angular
- **Indentation**: 4 spaces (via tabs)
- **Line Length**: 200 characters
- **Semicolons**: Always required
- **Quotes**: Single quotes preferred
- **Trailing Commas**: ES5 compatible
- **Arrow Functions**: Avoid parentheses when possible
- **HTML Attributes**: Tags with 3+ attributes formatted on multiple lines

## 🔍 Quality Checks

### What Gets Checked
1. **Code Formatting**: Prettier and Spotless ensure consistent style
2. **Line Length**: 200-character limit enforced
3. **Code Quality**: ESLint catches common issues
4. **Import Organization**: Automatic sorting and grouping
5. **License Headers**: Consistent copyright notices

### Exemptions
- URLs longer than 200 characters (ignored in ESLint)
- String literals and template literals (configurable)
- Regular expressions (ignored in ESLint)
- Comments (ignored in ESLint)

## 🚨 Troubleshooting

### Common Issues

#### "Spotless check failed"
```bash
cd whatsapp-backend
./gradlew spotlessApply
```

#### "Prettier formatting issues"
```bash
cd whatsapp-web
npm run format
```

#### "ESLint errors"
```bash
cd whatsapp-web
npm run lint:fix
```

#### Gradle permission issues
```bash
chmod +x whatsapp-backend/gradlew
```

### Build Integration

The formatting checks are integrated into the build process:
- `./gradlew build` will fail if Java code is not formatted
- `npm run build` includes linting checks
- CI/CD pipeline blocks merges if formatting issues exist

## 📚 Resources

- [Google Java Format](https://github.com/google/google-java-format)
- [Spotless Plugin](https://github.com/diffplug/spotless)
- [Prettier Configuration](https://prettier.io/docs/en/configuration.html)
- [ESLint Rules](https://eslint.org/docs/rules/)
- [Angular ESLint](https://github.com/angular-eslint/angular-eslint)
