A RESTful CRUD API for managing tasks, built as part of a hands-on Java/Spring Boot refresher project. Demonstrates a layered architecture with proper separation of concerns, database persistence, input validation, centralized error handling, unit-tested business logic, containerized deployment, caching, and JWT-based authentication with role-based authorization.

## Tech Stack

- **Java 21**
- **Spring Boot 4** (Spring Web MVC, Spring Data JPA, Spring Validation, Spring Security)
- **PostgreSQL** — persistent storage
- **Redis** — response caching
- **JWT (JJWT)** — stateless token-based authentication
- **Docker & Docker Compose** — containerized local development
- **JUnit 5 + Mockito** — unit testing
- **Maven** — build tool

The project follows a standard layered MVC architecture:
- **Controller** — handles HTTP requests/responses only, no business logic
- **Service** — contains business logic, orchestrates repository calls, handles DTO ↔ Entity conversion
- **Repository** — data access via Spring Data JPA (`JpaRepository`)
- **DTOs** (`TaskRequestDto`, `TaskResponse`, `LoginRequest`/`LoginResponse`, `RegisterRequestDto`/`RegisterResponseDto`, `ErrorResponse`) — used for incoming/outgoing API data, kept separate from internal entities to avoid exposing database structure (or sensitive fields like password hashes) directly through the API
- **Security** — `JwtUtil`, `CustomUserDetailsService`, and `JwtAuthenticationFilter` handle token generation/validation and request authentication

### Query Parameters (GET /tasks)
- `page` (default: 0)
- `size` (default: 10)
- `sortBy` (default: id)
- `completed` (optional, boolean)

## Authentication

The API uses **stateless JWT-based authentication** — no server-side sessions are created; every request must carry a valid token.

### Endpoints
- `POST /auth/register` — creates a new user account. Password is hashed with BCrypt before storage; usernames must be unique (`409 Conflict` if already taken).
- `POST /auth/login` — verifies credentials and returns a signed JWT on success (`401 Unauthorized` on invalid credentials).

### How it works
1. On login, a JWT is issued containing the username as its subject, signed with a server-side secret key.
2. For subsequent requests, the client includes the token in the `Authorization: Bearer <token>` header.
3. A custom `JwtAuthenticationFilter` validates the token on every request and reloads the user fresh from the database (rather than trusting the token's contents alone), so access reflects the user's *current* state — not just what was true at the moment of login.
4. Requests to `/auth/**` are public; all other endpoints require a valid token (`401 Unauthorized` if missing/invalid).

## Authorization

Two layers of authorization are enforced on top of authentication:

- **Ownership-based** — users can only view, update, or delete tasks they created. Attempting to access another user's task returns `404 Not Found` (deliberately indistinguishable from a non-existent task, to avoid leaking which task IDs exist to unauthorized users).
- **Role-based** — users have a `role` (`USER` or `ADMIN`). Admins can bypass ownership restrictions to view and manage any user's tasks via a dedicated `GET /tasks/admin/all` endpoint, enforced with Spring Security's `@PreAuthorize("hasRole('ADMIN')")` at the method level.

## Running with Docker

The full stack (app + PostgreSQL + Redis) runs via Docker Compose — no local Postgres/Redis installation required.

### Setup
1. Create a `.env` file in the project root with the following variables:
    - DB_PASSWORD=<your chosen password>
    - JWT_SECRET=<a long, random base64-encoded string>

2. Build and start the stack:
```bash
   docker-compose up --build
```
3. The API is available at `http://localhost:8080`.

### Notes
- Postgres data persists across restarts via a named volume. Changed `DB_PASSWORD`? Run `docker-compose down -v` to reset it.