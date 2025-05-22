# WhatsApp Clone – Backend

This is the backend for a WhatsApp-like web application, built with **Spring Boot 3.x**, **Spring Security (JWT)**, **Spring Data JPA**, **WebSocket (STOMP)**, and **PostgreSQL**.  
**Note:** This project is developed by me to polish and deepen my skills in the technologies used here.

---

## 🚀 Project Purpose

This backend is part of a full-stack WhatsApp clone, designed to deliver a streamlined, real-time messaging experience.  
**I am developing this project to polish my skills in Java, Spring Boot, WebSocket, PostgreSQL, and related backend technologies.**

---

## 📦 Project Structure

```
whatsapp-backend/
│
├── src/...
├── dependencies/
│   ├── docker-compose.yml
│   ├── dev-database.env
│   └── dev-init.sql
├── application.env
├── build.gradle.kts
└── ...
```

---

## ⚙️ Prerequisites

-   Docker (for PostgreSQL container)
-   Java 17+ (for Spring Boot)
-   Gradle (wrapper included)
-   (Optional) PostgreSQL client for DB inspection

---

## 1️⃣ Environment Configuration

All environment variables are managed in [`application.env`](application.env):

```env
DATABASE_HOST=localhost
DATABASE_NAME=whatsapp_db
DATABASE_PORT=5432
DATABASE_USER=postgres
DATABASE_PASSWORD=password
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION=3600000
FRONTEND_URL=http://localhost:4200
ACTIVE_PROFILE=dev
```

**Never commit real secrets or production credentials to version control.**

---

## 2️⃣ Start PostgreSQL with Docker Compose

From the `whatsapp-backend/dependencies` directory, run:

```bash
cd whatsapp-backend/dependencies
docker compose up -d
```

-   This starts a PostgreSQL container using settings from `dev-database.env`.
-   Data is persisted in a Docker volume.
-   The DB is accessible at `localhost:5432` with credentials from `application.env`.

---

## 3️⃣ Database Initialization & Migrations

-   **Liquibase** is used for schema migrations (see `src/main/resources/db/changelog/`).
-   On first run, the database is initialized automatically.
-   For development, the `DataSeeder` class seeds mock users, conversations, and messages (active on `dev` profile).

---

## 4️⃣ Running the Backend

From the `whatsapp-backend` directory:

```bash
./gradlew bootRun
```

Or build and run:

```bash
./gradlew build
java -jar build/libs/whatsapp-backend-*.jar
```

API base URL: [http://localhost:8080/api](http://localhost:8080/api)

---

## 5️⃣ API Security

-   All endpoints are protected with JWT authentication.
-   JWT secret and expiration are set in `application.env`.
-   CORS is configured for the frontend URL.

---

## 6️⃣ Useful Commands

-   **Stop the database:**
    ```bash
    docker compose down
    ```
-   **Remove all data:**
    ```bash
    docker compose down -v
    ```
-   **View DB logs:**
    ```bash
    docker compose logs postgres
    ```

---

## 7️⃣ Troubleshooting

-   If port 5432 is in use, change the port in `docker-compose.yml` and `application.env`.
-   If migrations fail, check logs and ensure the DB is empty or compatible.
-   For DB inspection, connect with:
    ```
    psql -h localhost -U postgres -d whatsapp_db
    ```

---

## 8️⃣ Development Best Practices

-   Use feature branches for new work.
-   Write unit and integration tests for all changes.
-   Use DTOs for all API responses; never expose entities directly.
-   All DTOs are wrapped in `ApiResponse` objects.
-   Use UUIDs for all IDs.
-   Keep secrets out of version control.

---

## 9️⃣ Project Status

-   Authentication system is fully implemented
-   Database schema is defined
-   Project structure is established
-   API endpoints are documented

---

## 📄 License

MIT

---

## 📣 Contact

For questions or contributions, open an issue or contact the maintainer.
