# Book Store — Backend

Spring Boot 3 REST API for the Book Store eCommerce application.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 17+ | [Adoptium JDK 17](https://adoptium.net/) recommended |
| Maven | 3.9+ | Or use the `./mvnw` wrapper |
| PostgreSQL | 18 | Database server must be running before starting the app |
| Git | Any | For source control |

Verify your setup:
```bash
java -version
mvn -version
psql --version
```

---

## Database Setup (PostgreSQL 18)

Create the database and user for local development:

```sql
CREATE DATABASE bookstore_dev;
CREATE USER bookstore WITH PASSWORD 'bookstore';
GRANT ALL PRIVILEGES ON DATABASE bookstore_dev TO bookstore;
```

> **Note:** The application uses `spring.jpa.hibernate.ddl-auto=create-drop` in the `dev` profile,
> so tables are created automatically on startup and dropped on shutdown.
> Flyway migrations will replace this in a later phase.

---

## Configuration

All configuration is managed via YAML files in `src/main/resources/`:

| File | Purpose |
|------|---------|
| `application.yml` | Base configuration — shared across all profiles |
| `application-dev.yml` | Local development overrides |
| `application-prod.yml` | Production overrides (uses environment variables) |

### Environment Variables (Production)

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://db:5432/bookstore` |
| `DB_USERNAME` | Database username | `bookstore` |
| `DB_PASSWORD` | Database password | `secret` |
| `JWT_SECRET` | JWT HMAC-SHA256 signing secret (min 32 chars) | `...` |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds | `86400000` |
| `SERVER_PORT` | HTTP port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |

---

## Running the Application

### Development (default)

```bash
cd backend
mvn spring-boot:run
```

The `dev` profile is activated by default via `application.yml`.

### With explicit profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production (JAR)

```bash
mvn clean package -DskipTests
java -jar target/book-store-backend-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://localhost:5432/bookstore \
  --DB_USERNAME=bookstore \
  --DB_PASSWORD=secret \
  --JWT_SECRET=your-very-long-secret-key
```

---

## Maven Commands

| Command | Description |
|---------|-------------|
| `mvn clean install` | Clean, compile, test, and package |
| `mvn clean package` | Build JAR without running tests again |
| `mvn clean package -DskipTests` | Build JAR, skip tests |
| `mvn spring-boot:run` | Start the application |
| `mvn test` | Run all tests |
| `mvn verify` | Run tests + integration checks |
| `mvn dependency:tree` | Show full dependency tree |

---

## Verification URLs

Once the application starts, verify it is working:

| URL | Description |
|-----|-------------|
| `http://localhost:8080/api/v1/health` | Health check endpoint |
| `http://localhost:8080/swagger-ui.html` | Swagger UI (interactive API docs) |
| `http://localhost:8080/api-docs` | Raw OpenAPI JSON |
| `http://localhost:8080/actuator/health` | Spring Boot Actuator health |

Expected health endpoint response:
```json
{
  "success": true,
  "data": {
    "status": "UP",
    "application": "book-store-backend",
    "version": "1.0.0-SNAPSHOT",
    "timestamp": "2025-07-21T10:00:00Z",
    "port": "8080"
  },
  "message": "Book Store API is running"
}
```

---

## Project Structure

```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/bookstore/
    │   │   ├── BookStoreApplication.java       # Spring Boot entry point
    │   │   ├── config/                         # Configuration beans
    │   │   │   ├── AppProperties.java          # Typed app.* config
    │   │   │   ├── SecurityConfig.java         # Spring Security
    │   │   │   ├── SwaggerConfig.java          # OpenAPI / Swagger UI
    │   │   │   └── WebMvcConfig.java           # CORS
    │   │   ├── common/
    │   │   │   ├── constants/AppConstants.java # App-wide constants
    │   │   │   ├── exception/                  # Custom exceptions + GlobalExceptionHandler
    │   │   │   ├── response/                   # ApiResponse + PagedResponse
    │   │   │   └── util/                       # PriceCalculator, DeliveryDateCalculator
    │   │   ├── controller/
    │   │   │   └── HealthController.java       # GET /api/v1/health
    │   │   ├── service/                        # Business logic (to be implemented)
    │   │   ├── repository/                     # Spring Data JPA repos (to be implemented)
    │   │   ├── domain/                         # JPA entities (to be implemented)
    │   │   │   └── enums/                      # BookFormat, OrderStatus, etc.
    │   │   ├── dto/
    │   │   │   ├── request/                    # Request DTOs (to be implemented)
    │   │   │   └── response/                   # Response DTOs (to be implemented)
    │   │   ├── mapper/                         # MapStruct mappers (to be implemented)
    │   │   └── security/                       # JWT filter, UserPrincipal (to be implemented)
    │   └── resources/
    │       ├── application.yml                 # Base config
    │       ├── application-dev.yml             # Dev overrides
    │       └── application-prod.yml            # Prod overrides
    └── test/
        └── java/com/bookstore/
            └── BookStoreApplicationTests.java  # Context load test
```

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.2 |
| Security | Spring Security 6 |
| Data Access | Spring Data JPA + Hibernate |
| Database | PostgreSQL 18 |
| Connection Pool | HikariCP |
| API Docs | Springdoc OpenAPI 3 (Swagger UI) |
| JWT | JJWT 0.12.6 |
| Object Mapping | MapStruct 1.5.5 |
| Boilerplate | Lombok |
| Build | Apache Maven 3.9+ |

---

*Book Store eCommerce Application — AI-Assisted Development with IBM Bob*
