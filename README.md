# WhatsApp Clone – Full Stack Project

[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=ahmedmirza994_whatsapp&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ahmedmirza994_whatsapp)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ahmedmirza994_whatsapp&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ahmedmirza994_whatsapp)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=ahmedmirza994_whatsapp&metric=bugs)](https://sonarcloud.io/summary/new_code?id=ahmedmirza994_whatsapp)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=ahmedmirza994_whatsapp&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=ahmedmirza994_whatsapp)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ahmedmirza994_whatsapp&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=ahmedmirza994_whatsapp)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ahmedmirza994_whatsapp&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=ahmedmirza994_whatsapp)

This repository contains a full-stack WhatsApp-like web application, built with a **Spring Boot (Java)** backend and an **Angular 19** frontend.  
**Note:** This project is developed by me to polish and deepen my skills in modern web technologies, real-time communication, and scalable application architecture.

---

## 📚 Project Overview

The WhatsApp Clone aims to deliver a streamlined, real-time messaging experience with a clean, mobile-first UI and robust backend.  
It demonstrates best practices in authentication, WebSocket-based messaging, modular code structure, and secure, scalable design.

---

## 🏗️ Repository Structure

```
whatsapp/
│
├── whatsapp-backend/   # Spring Boot backend (Java, PostgreSQL, WebSocket)
├── whatsapp-web/       # Angular 19 frontend (RxJS, Signals, STOMP)
└── README.md           # (You are here)
```

---

## 🚀 MVP Feature Set

-   **User Authentication:** JWT-secured login/signup, password encryption
-   **Messaging System:** One-to-one chat, timestamps, soft delete
-   **User Discovery:** Search, add contacts, view profiles
-   **Real-time Communication:** WebSocket (STOMP) for instant messaging, typing indicators, read receipts
-   **Profile & Settings:** Manage profile, update picture
-   **Message & Conversation Deletion:** Soft delete for messages/conversations
-   **Conversation Read Receipts:** "Seen" status, last seen
-   **Typing Indicators:** Real-time typing status

---

## 🛠️ Technical Stack

-   **Backend:** Spring Boot 3.x, Spring Security (JWT), Spring Data JPA, WebSocket (STOMP), PostgreSQL, RESTful API
-   **Frontend:** Angular 19, RxJS, Angular Signals, SockJS/STOMP, SCSS/CSS

---

## 🎨 UI/UX Theme

-   **Background:** #f5f7fa (soft, clean)
-   **Sent Bubble:** #3498db (blue, white text)
-   **Received Bubble:** #e0e6ed (neutral, dark text)
-   **Input:** Minimal, blue send button
-   **Meta Info:** Muted gray, small font
-   **Responsive:** Mobile-first, accessible, modern chat design

---

## 🔒 Security & Best Practices

-   JWT authentication for all API endpoints
-   Secure WebSocket connections
-   Input validation (client & server)
-   XSS and CSRF protection
-   Proper error handling (no sensitive info exposed)
-   DTO pattern for all API responses
-   Feature branches, unit/integration tests, and code reviews

---

## 🚀 CI/CD & Code Quality

This project uses **GitHub Actions** for automated testing, code quality analysis, and continuous integration.

### 📊 Automated Workflows

-   **🔍 SonarCloud Analysis**: Comprehensive code quality, security, and coverage analysis
-   **🧪 Build & Test**: Automated testing with coverage reporting
-   **📈 Quality Gates**: Enforced quality standards for all pull requests
-   **💬 PR Comments**: Automatic code coverage and quality reports on pull requests

### ⚡ Workflow Triggers

-   **Push to `main`**: Full analysis and quality reporting
-   **Pull Requests to `main`**: Code quality checks with PR comments
-   **Backend Changes**: Workflows only run when backend code is modified

### 📋 What You Get on Pull Requests

-   ✅ **Automated Code Review**: Quality gate pass/fail status
-   📊 **Coverage Report**: Detailed coverage for overall project and changed files
-   🐛 **Bug Detection**: Automatic identification of potential bugs
-   🔒 **Security Scan**: Security vulnerability and hotspot analysis
-   👃 **Code Smell Detection**: Maintainability and clean code suggestions
-   💬 **Inline Comments**: Direct feedback on code quality issues

### 🎯 Quality Standards

-   **Minimum Coverage**: 70% overall, 80% for changed files
-   **Quality Gate**: Must pass all SonarCloud quality checks
-   **Security**: Zero tolerance for security vulnerabilities
-   **Code Smells**: Maintained at minimal levels

### 🔗 Quality Dashboard

-   [📊 SonarCloud Project](https://sonarcloud.io/project/overview?id=ahmedmirza994_whatsapp)
-   [📋 Coverage Report](https://sonarcloud.io/component_measures?id=ahmedmirza994_whatsapp&metric=coverage)
-   [🔒 Security Analysis](https://sonarcloud.io/project/security_hotspots?id=ahmedmirza994_whatsapp)

### 📁 CI/CD Documentation

For detailed information about the CI/CD setup, workflows, and GitHub Actions configuration, see [CI/CD Setup Guide](.github/CI-CD-SETUP.md).

### 🛠️ Local Quality Checks

Run the same quality checks locally before pushing:

```bash
# Run all quality checks locally
./check-quality.sh

# Or run individual steps
cd whatsapp-backend
./gradlew clean test jacocoTestReport
./run-sonar.sh  # Requires SONAR_TOKEN
```

### 🔄 Development Workflow

1. **Create Feature Branch**: `git checkout -b feature/your-feature`
2. **Run Local Checks**: `./check-quality.sh`
3. **Push & Create PR**: Automated quality checks will run
4. **Review Results**: Check PR comments for coverage and quality feedback
5. **Merge**: Only when all quality gates pass ✅

---

## 📦 Getting Started

See the individual READMEs for setup and usage:

-   [Backend Setup](whatsapp-backend/README.md)
-   [Frontend Setup](whatsapp-web/README.md)

---

## 📈 Project Status

-   Authentication, messaging, and profile management are fully implemented
-   Real-time chat, typing indicators, and read receipts are live
-   Database schema and API contracts are defined
-   Project structure follows industry best practices

---

## 📝 License

MIT

---

## 🙋‍♂️ About

This project is for educational purposes and skill development in modern full-stack development.  
For questions or contributions, please open an issue or contact the maintainer.
