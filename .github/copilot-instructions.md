# WhatsApp Clone Project Instructions

## Project Overview

This project is a WhatsApp-like web application built with Spring Boot (Java) backend and Angular 19 frontend. The application focuses on delivering a streamlined messaging experience with real-time capabilities.

## MVP Feature Set

-   **User Authentication** ✅

    -   Login and signup functionality
    -   JWT security for all backend endpoints
    -   Password encryption and secure session management

-   **Messaging System** 🔄

    -   One-to-one conversations
    -   Text message exchange
    -   Message timestamps

-   **User Discovery** 🔄

    -   Search users by name, email, or phone number
    -   Add contacts to conversation list
    -   User profile information display

-   **Real-time Communication** 🔄
    -   WebSocket implementation for instant message delivery

## Technical Stack

-   **Backend**

    -   Spring Boot 3.x
    -   Spring Security with JWT
    -   Spring Data JPA
    -   WebSocket (STOMP protocol)
    -   PostgreSQL database
    -   RESTful API design

-   **Frontend**
    -   Angular 19
    -   RxJS for reactive programming
    -   Angular Signals (preferred over NgRx)
    -   SockJS/STOMP for WebSocket client

## Code Style Guidelines

-   **Backend**

    -   Use UUID for any type of ids
    -   Use lowercase sql statements
    -   Use Spring best practices with dependency injection
    -   Follow REST API conventions
    -   Implement proper exception handling and validation
    -   Create comprehensive unit and integration tests
    -   Use DTO patterns for request/response objects
    -   All database entites goes to entity package and then similar model classes goes to model package
    -   Model will not directly expose to FE, create DTO for this.

-   **Frontend**
    -   Component-based architecture
    -   Lazy loading modules
    -   Reactive forms with validation
    -   Consistent color scheme ( #3498db for buttons, #f5f7fa / #c3cfe2 for gradients, #333333 for text)
    -   Mobile-first responsive design

## Security Requirements

-   JWT authentication for all API endpoints
-   Secure WebSocket connections
-   Input validation on both client and server
-   XSS and CSRF protection
-   Proper error handling without exposing sensitive information

## Current Project Status

-   Authentication system is fully implemented (backend and frontend)
-   Database schema is defined
-   Project structure is established
-   API endpoints are documented
