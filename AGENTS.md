# Barbilling Project - Agent Guidelines

## Project Overview

- **Project Name**: Barbilling
- **Type**: Spring Boot REST API (Java 21)
- **Framework**: Spring Boot 4.0.4
- **Database**: PostgreSQL (localhost:5432/barbilling)
- **Port**: 8080

## Build Commands

### Using Maven Wrapper (mvnw.cmd on Windows)

```bash
# Build the project
./mvnw.cmd clean package

# Compile without tests
./mvnw.cmd compile

# Run the application
./mvnw.cmd spring-boot:run

# Run all tests
./mvnw.cmd test

# Run a single test class
./mvnw.cmd test -Dtest=BarbillingApplicationTests

# Run a single test method
./mvnw.cmd test -Dtest=BarbillingApplicationTests#contextLoads

# Run tests with verbose output
./mvnw.cmd test -Dsurefire.useFile=false
```

## Code Style Guidelines

### Package Structure

```
src/main/java/com/jsalvar/barbilling/
├── aspect/          # AOP aspects (e.g., @Loggable)
├── config/          # Spring configuration classes
├── controller/      # REST controllers
│   └── advise/     # Controller advice for exceptions
├── dto/
│   ├── request/    # Request DTOs
│   └── response/  # Response DTOs
├── entity/         # JPA entities
├── exception/      # Custom exceptions
├── filter/         # Servlet filters
├── repository/     # Spring Data repositories
└── service/        # Business logic
    └── impl/      # Service implementations
```

### Naming Conventions

- **Controllers**: `*Controller.java` (e.g., `UserController.java`)
- **Services**: `*Service.java` (interface), `*ServiceImpl.java` (implementation)
- **Repositories**: `*Repository.java` (extends `JpaRepository`)
- **DTOs**: `*RequestDto.java`, `*ResponseDto.java` (Java records preferred)
- **Entities**: `*Impl.java` for implementations, simple names for enums
- **Exceptions**: `*Exception.java`

### DTOs (Data Transfer Objects)

Use Java records for immutable DTOs:

```java
public record UserResponseDto(
        String id,
        String name,
        String lastname,
        String email,
        Role role
) {}
```

### Entities

- Use `@Entity` and `@Table` annotations
- Use Lombok `@Data` for getters/setters
- UUID for IDs with `@GeneratedValue(strategy = GenerationType.UUID)`
- Use enums with `@Enumerated(EnumType.STRING)`
- Enums located in `entity/enums/` package

```java
@Entity
@Table(name = "users")
@Data
public class UserImpl implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Enumerated(EnumType.STRING)
    private Role role;
}
```

### Entity Reference

| Entity | Description | Key Fields |
|--------|-------------|------------|
| `UserImpl` | User authentication | id, name, lastname, email, password, role |
| `Category` | Product categories | id, name, kitchenType, taxRates |
| `Product` | Menu items | id, name, description, price, available, category |
| `BarTable` | Restaurant tables | id, number, capacity, status |
| `Tab` | Open orders per table | id, table, waiter, status, openedAt, closedAt |
| `OrderItem` | Items in a tab | id, quantity, unitPrice, notes, product, tab |
| `Bill` | Payment records | id, tab, cashier, total, tip, paymentMethod, paymentStatus, paidAt |
| `Stock` | Inventory tracking | id, product, quantity, lowStockThreshold |
| `TaxRate` | Tax percentages | id, name, rate |

### Enums

| Enum | Values |
|------|--------|
| `Role` | ADMIN, CASHIER, WAITER |
| `KitchenType` | BAR, KITCHEN |
| `TableStatus` | FREE, OCCUPIED, RESERVED |
| `TabStatus` | OPEN, CLOSED, CANCELLED |
| `PaymentMethod` | CASH, CARD, TRANSFER |
| `PaymentStatus` | PENDING, PAID, CANCELLED |

### Controllers

- Use `@RestController` and `@RequestMapping`
- Annotate with `@Loggable` for method logging
- Return `ResponseEntity<T>` for consistent responses
- Use proper HTTP status codes

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Loggable
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        // implementation
    }
}
```

### Services

- Define service interfaces in `service/` package
- Implement in `service/impl/` package
- Use constructor injection (no `@Autowired` on fields)

```java
public interface UserService {
    List<UserImpl> findAll();
    UserImpl findById(UUID id);
}

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### Error Handling

- Use `@ControllerAdvice` for global exception handling
- Create custom exceptions in `exception/` package
- Return consistent error response DTOs

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ErrorResponseDto> handleUnprocessableEntity(UnprocessableEntityException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponseDto(...));
    }
}
```

### Security

- JWT-based authentication
- Stateless sessions (`SessionCreationPolicy.STATELESS`)
- Public endpoints under `/auth/**`
- All other endpoints require authentication
- Passwords encrypted with BCrypt

### Logging

- Use `@Loggable` annotation for automatic method entry/exit logging
- Use SLF4J `Logger` and `LoggerFactory`

### Testing

- Use JUnit 5 (`org.junit.jupiter.api.Test`)
- Use `@SpringBootTest` for integration tests
- Test location: `src/test/java/`

### Imports Organization

Order imports:
1. Java/Jakarta EE
2. Spring Framework
3. Third-party libraries
4. Project imports

### General Practices

- **No comments** unless explicitly requested
- Use constructor injection, not field injection
- Keep methods small and focused
- Use meaningful variable and method names
- Follow RESTful conventions for endpoints (`/api/v1/resource`)
- Use records for immutable DTOs
- Use Lombok to reduce boilerplate

## API Endpoints

### Auth
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login (returns JWT token)

### Users (requires JWT)
- `GET /api/v1/users` - Get all users
- `GET /api/v1/users/{id}` - Get user by ID

### Categories (requires JWT)
- `GET /api/v1/categories` - Get all categories
- `GET /api/v1/categories/{id}` - Get category by ID

### Products (requires JWT)
- `GET /api/v1/products` - Get all products
- `GET /api/v1/products/{id}` - Get product by ID

### Tables (requires JWT)
- `GET /api/v1/tables` - Get all tables
- `GET /api/v1/tables/{id}` - Get table by ID

### Tabs (requires JWT)
- `GET /api/v1/tabs` - Get all tabs
- `GET /api/v1/tabs/{id}` - Get tab by ID

### Bills (requires JWT)
- `GET /api/v1/bills` - Get all bills
- `GET /api/v1/bills/{id}` - Get bill by ID

## Database

- PostgreSQL on localhost:5432
- Database name: barbilling
- Username: postgres / Password: postgres
- Hibernate DDL: update (not create-drop in production)
