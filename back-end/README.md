# Slotify Backend

Spring Boot backend for the Slotify scheduling system for barbershops and salons.

## Technology Stack

- **Java 17** (LTS)
- **Spring Boot 3.2.0**
- **Spring Data JPA** with Hibernate
- **PostgreSQL** database
- **Gradle 8.5** build system
- **JUnit 5** for testing

## Project Structure

```
src/
├── main/
│   ├── java/com/slotify/
│   │   ├── SlotifyApplication.java          # Main Spring Boot application
│   │   ├── config/                          # Configuration classes
│   │   │   ├── DatabaseConfig.java         # Database configuration
│   │   │   └── WebConfig.java              # Web/CORS configuration
│   │   ├── controller/                      # REST controllers
│   │   │   └── HealthController.java       # Health check endpoint
│   │   ├── service/                         # Business logic services
│   │   │   └── BaseService.java            # Base service interface
│   │   ├── repository/                      # Data access layer
│   │   │   └── BaseRepository.java         # Base repository interface
│   │   ├── model/                           # JPA entities
│   │   │   └── BaseEntity.java             # Base entity with audit fields
│   │   └── exception/                       # Exception handling
│   │       ├── SlotifyException.java       # Base exception
│   │       ├── ResourceNotFoundException.java
│   │       └── GlobalExceptionHandler.java # Global exception handler
│   └── resources/
│       ├── application.properties          # Main configuration
│       ├── application-dev.properties      # Development profile
│       └── application-prod.properties     # Production profile
└── test/
    ├── java/com/slotify/
    │   └── SlotifyApplicationTests.java    # Basic integration test
    └── resources/
        └── application-test.properties     # Test configuration (H2 database)
```

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL database

### Database Setup

1. Install PostgreSQL
2. Create databases:
   ```sql
   CREATE DATABASE slotify_dev;
   CREATE DATABASE slotify_prod;
   ```

### Running the Application

1. **Development mode:**
   ```bash
   ./gradlew bootRun
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run tests:**
   ```bash
   ./gradlew test
   ```

### Configuration

The application uses Spring profiles for different environments:

- `dev` - Development (default)
- `prod` - Production  
- `test` - Testing (uses H2 in-memory database)

### API Endpoints

- `GET /api/health` - Health check endpoint

### Database Configuration

The application is configured to use PostgreSQL with the following default settings:

- **Development:** `jdbc:postgresql://localhost:5432/slotify_dev`
- **Production:** Uses environment variables for connection details

### Building for Production

```bash
./gradlew build
java -jar build/libs/slotify-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Development Notes

- The application uses Hibernate with `validate` DDL mode by default
- Development profile uses `update` DDL mode for easier development
- All entities should extend `BaseEntity` for audit trail functionality
- Global exception handling is configured for consistent API responses
- CORS is configured to allow frontend connections from `http://localhost:3000`