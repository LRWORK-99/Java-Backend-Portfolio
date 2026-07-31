A RESTful CRUD API for managing tasks, built as part of a hands-on Java/Spring Boot refresher project. Demonstrates a layered architecture with proper separation of concerns, database persistence, input validation, centralized error handling, and unit-tested business logic.

## Tech Stack

- **Java 21**
- **Spring Boot 4** (Spring Web MVC, Spring Data JPA, Spring Validation)
- **PostgreSQL** — persistent storage
- **JUnit 5 + Mockito** — unit testing
- **Maven** — build tool

The project follows a standard layered MVC architecture:
- **Controller** — handles HTTP requests/responses only, no business logic
- **Service** — contains business logic, orchestrates repository calls, handles DTO ↔ Entity conversion
- **Repository** — data access via Spring Data JPA (`JpaRepository`)
- **DTOs** (`TaskRequestDto`) — used for incoming request data, kept separate from the internal `Task` entity to avoid exposing database structure directly through the API

### Query Parameters (GET /tasks)
- `page` (default: 0)
- `size` (default: 10)
- `sortBy` (default: id)
- `completed` (optional, boolean)