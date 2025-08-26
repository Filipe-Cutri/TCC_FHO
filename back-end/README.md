# Slotfy Backend

Spring Boot backend for the Slotfy scheduling system for barbershops and salons.

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
│   ├── java/com/slotfy/
│   │   ├── SlotfyApplication.java          # Main Spring Boot application
│   │   ├── config/                          # Configuration classes
│   │   │   ├── DatabaseConfig.java         # Database configuration
│   │   │   ├── SecurityConfig.java         # Security configuration
│   │   │   └── WebConfig.java              # Web/CORS configuration
│   │   ├── controller/                      # REST controllers (API endpoints)
│   │   │   ├── AppointmentController.java  # Appointment management
│   │   │   ├── ClientAuthController.java   # Client authentication
│   │   │   ├── EstablishmentAuthController.java # Establishment auth
│   │   │   ├── HealthController.java       # Health check endpoint
│   │   │   └── ...                         # Other controllers
│   │   ├── service/                         # Business logic services
│   │   │   ├── AppointmentService.java     # Appointment business logic
│   │   │   ├── ClientService.java          # Client business logic
│   │   │   ├── BaseService.java            # Base service interface
│   │   │   └── ...                         # Other services
│   │   ├── repository/                      # Data access layer (JPA/Hibernate)
│   │   │   ├── AppointmentRepository.java  # Appointment data access
│   │   │   ├── ClientRepository.java       # Client data access
│   │   │   ├── BaseRepository.java         # Base repository interface
│   │   │   └── ...                         # Other repositories
│   │   ├── model/                           # JPA entities (database tables)
│   │   │   ├── Appointment.java            # Appointment entity
│   │   │   ├── Client.java                 # Client entity
│   │   │   ├── Establishment.java          # Establishment entity
│   │   │   ├── BaseEntity.java             # Base entity with audit fields
│   │   │   └── ...                         # Other entities
│   │   ├── dto/                             # Data Transfer Objects (API input/output)
│   │   │   ├── ClientLoginRequest.java     # Client login request DTO
│   │   │   ├── ClientResponse.java         # Client response DTO
│   │   │   ├── AppointmentCreateRequest.java # Appointment creation DTO
│   │   │   ├── ApiResponse.java            # Generic API response wrapper
│   │   │   └── ...                         # Other DTOs
│   │   └── exception/                       # Exception handling
│   │       ├── SlotfyException.java       # Base exception
│   │       ├── ResourceNotFoundException.java
│   │       └── GlobalExceptionHandler.java # Global exception handler
│   └── resources/
│       ├── application.properties          # Main configuration
│       ├── application.yml                 # YAML configuration (alternative)
│       ├── application-dev.properties      # Development profile
│       ├── application-prod.properties     # Production profile
│       ├── application-test.properties     # Test profile
│       ├── static/                         # Static files (if embedding frontend)
│       └── templates/                      # Thymeleaf templates (if using SSR)
└── test/                                   # Unit and integration tests
    ├── java/com/slotfy/
    │   └── SlotfyApplicationTests.java    # Basic integration test
    └── resources/
        └── application-test.properties     # Test configuration (H2 database)
```

## Spring Boot Best Practices

This project follows Spring Boot best practices with the following structure:

### 📂 **controller/** → API Endpoints
- REST controllers that handle HTTP requests
- Endpoint mapping (e.g., `/api/appointments`)
- Request/response handling using DTOs

### 📂 **service/** → Business Logic
- Service layer containing business rules and logic
- Transaction management
- Data validation and processing

### 📂 **repository/** → Database Access
- Data access layer using JPA/Hibernate
- Custom query methods
- Database operations abstraction

### 📂 **model/** → JPA Entities  
- Entity classes representing database tables
- JPA annotations and relationships
- Audit fields through BaseEntity

### 📂 **dto/** → Data Transfer Objects
- Input/output objects for API communication
- Request validation using Jakarta Bean Validation
- Type-safe API contracts

### 📂 **config/** → Configuration Classes
- Security configuration (CORS, authentication)
- Database configuration
- Application-wide settings

### 📂 **resources/** → Application Configuration
- `application.yml` / `application.properties` - Main configuration
- Profile-specific configurations (dev, prod, test)
- Static files and templates (if needed)

### 📂 **test/** → Unit and Integration Tests
- Test classes organized by layer
- Integration tests for controllers and services
- Test configuration with H2 database

## Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL database

### Database Setup

1. Install PostgreSQL
2. Create databases:
   ```sql
   CREATE DATABASE slotfy_dev;
   CREATE DATABASE slotfy_prod;
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

- **Development:** `jdbc:postgresql://localhost:5432/slotfy_dev`
- **Production:** Uses environment variables for connection details

### Building for Production

```bash
./gradlew build
java -jar build/libs/slotfy-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Development Notes

- The application uses Hibernate with `validate` DDL mode by default
- Development profile uses `update` DDL mode for easier development
- All entities should extend `BaseEntity` for audit trail functionality
- Global exception handling is configured for consistent API responses
- CORS is configured to allow frontend connections from `http://localhost:3000`