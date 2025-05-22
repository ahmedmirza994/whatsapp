# WhatsApp Clone – Full Stack Project

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
