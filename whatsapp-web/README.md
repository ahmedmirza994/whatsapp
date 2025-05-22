# WhatsApp Clone – Frontend (Angular 19)

This is the **frontend** for a WhatsApp-like web application, built with **Angular 19**, **RxJS**, **Angular Signals**, and **SockJS/STOMP** for real-time messaging.  
**Note:** I am developing this project to polish and deepen my skills in modern Angular, TypeScript, RxJS, and real-time web technologies.

---

## 🚀 Project Purpose

This frontend delivers a streamlined, mobile-first messaging experience, supporting real-time chat, user discovery, profile management, and more.  
It is part of a full-stack WhatsApp clone, with a Spring Boot backend.

---

## 📦 Project Structure

```
whatsapp-web/
│
├── src/
│   ├── app/
│   │   ├── auth/                # Login, signup, authentication logic
│   │   ├── chat/                # Chat UI, conversation, message area, profile settings
│   │   ├── shared/              # Shared models, services, components
│   │   └── quote/               # Motivational quote component
│   ├── assets/
│   ├── environments/
│   └── styles.css
├── angular.json
├── package.json
└── ...
```

---

## ⚙️ Prerequisites

- Node.js 18+
- npm 9+ (or yarn)
- [Angular CLI](https://angular.io/cli) (recommended, but not required)

---

## 1️⃣ Environment Setup

1. **Install dependencies:**

    ```bash
    cd whatsapp-web
    npm install
    ```

2. **Environment variables:**  
   Edit `src/environments/environment.ts` for API URLs and WebSocket endpoints as needed.

---

## 2️⃣ Running the App Locally

```bash
npm start
```

or

```bash
ng serve
```

- The app will be available at [http://localhost:4200](http://localhost:4200).
- Make sure the backend is running at the URL configured in your environment file.

---

## 3️⃣ MVP Features

- **User Authentication:** Login, signup, JWT session, password encryption
- **Messaging System:** One-to-one chat, timestamps, soft delete
- **User Discovery:** Search users, add contacts, view profiles
- **Real-time Communication:** WebSocket (STOMP) for instant messaging, typing indicators, read receipts
- **Profile & Settings:** Update profile, change picture
- **Responsive UI:** Mobile-first, accessible, modern chat design

---

## 4️⃣ Code Style & Best Practices

- **Component-based architecture** with standalone components
- **Lazy loading** for chat modules
- **Reactive forms** with validation
- **Signals** for state management (preferred over NgRx)
- **Strict TypeScript** and Angular compiler options
- **Service-based API calls** with RxJS
- **DTOs** for all backend communication

---

## 5️⃣ Security

- JWT authentication for all API requests
- Secure WebSocket connections
- Input validation on client and server
- XSS and CSRF protection
- Proper error handling (no sensitive info exposed)

---

## 6️⃣ Theming & UI Guidelines

- **Background:** `#f5f7fa`
- **Text:** `#333333`
- **Sent Bubble:** `#3498db` (white text, right-aligned, rounded)
- **Received Bubble:** `#e0e6ed` (dark text, left-aligned, rounded)
- **Input:** White background, border `#c3cfe2` or `#d1d5db`, send button `#3498db`
- **Meta Info:** Muted gray `#7f8c8d`, small font
- **Accessibility:** High contrast, responsive, keyboard navigable

---

## 7️⃣ Testing

```bash
npm test
```

- Uses Jasmine and Karma for unit testing.

---

## 8️⃣ Development Best Practices

- Use feature branches and PRs
- Write unit/integration tests for all changes
- Keep business logic in services, not components
- Use DTOs for all API requests/responses
- Avoid direct state mutation; use signals and RxJS

---

## 9️⃣ Project Status

- Authentication, chat, and profile features are implemented
- Real-time messaging and typing indicators are live
- UI is responsive and mobile-friendly

---

## 📄 License

MIT

---

## 📣 Contact

For questions or contributions, open an issue or contact the maintainer.
