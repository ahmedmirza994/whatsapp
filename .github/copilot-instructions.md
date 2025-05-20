# WhatsApp Clone Project Instructions

## Project Overview

This project is a WhatsApp-like web application built with Spring Boot (Java) backend and Angular 19 frontend. The application focuses on delivering a streamlined messaging experience with real-time capabilities.

## MVP Feature Set

-   **User Authentication** ✅

    -   Login and signup functionality
    -   JWT security for all backend endpoints
    -   Password encryption and secure session management

-   **Messaging System** ✅

    -   One-to-one conversations
    -   Text message exchange
    -   Message timestamps

-   **User Discovery** ✅

    -   Search users by name, email, or phone number
    -   Add contacts to conversation list
    -   User profile information display

-   **Real-time Communication** ✅

    -   WebSocket implementation for instant message delivery

-   **Profile & Settings** ✅

    -   User profile management
    -   Update profile picture

-   **Message Deletion** ✅

    -   Delete messages from conversations
    -   Delete entire conversations
    -   Soft delete implementation (messages are not permanently deleted)

-   **Conversation Read Receipts** ✅

    -   Display "Seen" status for messages
    -   Timestamps for sent and received messages
    -   Last seen status for users

-   **Typing Indicators** ✅
    -   Show when a user is typing in a conversation

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
    -   Wrap Dtos inside ApiResponse and then return to FE

-   **Frontend**
    -   Component-based architecture
    -   Lazy loading modules
    -   Reactive forms with validation
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

🎨 Chat Application Theme Instructions

1. General Background
   • Background Color: #f5f7fa
   • Text Color: #333333

Use the same soft, clean background as the login/signup screens for consistency.

⸻

2. Chat Bubbles

Sent Messages (User’s Own Messages)
• Background Color: #3498db
• Text Color: #ffffff
• Style: Rounded corners (e.g., 16px radius)

Sent messages should stand out clearly with a strong blue background and white text.

Received Messages (Other User’s Messages)
• Background Color: #e0e6ed
• Text Color: #333333
• Style: Rounded corners (e.g., 16px radius)

Received messages should be soft and neutral, maintaining readability and visual balance.

⸻

3. Chat Input Area
   • Input Field Background: #ffffff
   • Input Field Border Color: #c3cfe2 or #d1d5db
   • Input Text Color: #333333
   • Send Button Background: #3498db
   • Send Button Icon/Text Color: #ffffff

The input area should feel minimal and inviting, with the send button in your primary blue for clear action focus.

⸻

4. Timestamps and Meta Information
   • Text Color: #7f8c8d
   • Font Size: Small (e.g., 12px)
   • Placement: Below or beside messages, subtly styled.

Timestamps like “12:45 PM” or meta-info like “Seen” should appear in a muted gray to avoid distracting from the conversation.

⸻

5. Optional Styling Tips
   • Use slightly different bubble padding for sent vs. received messages (e.g., sent messages aligned to the right, received to the left).
   • Add a slight shadow (box-shadow: 0px 2px 4px rgba(0,0,0,0.05)) to bubbles for a more modern “floating” feel.
   • Ensure responsive behavior — bubbles should shrink or expand based on message length.
   • For better accessibility, maintain sufficient contrast between text and bubble background colors.

Please ensure use whole project as a context on every request.
