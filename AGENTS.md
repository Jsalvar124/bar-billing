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

### Test Folder Structure

```
src/test/java/com/jsalvar/barbilling/
├── user/
│   ├── UserServiceImplTest.java
│   └── UserControllerTest.java
├── auth/
│   ├── AuthServiceImplTest.java
│   └── AuthControllerTest.java
├── category/
│   ├── CategoryServiceImplTest.java
│   └── CategoryControllerTest.java
├── table/
│   ├── BarTableServiceImplTest.java
│   └── BarTableControllerTest.java
├── taxrate/
│   ├── TaxRateServiceImplTest.java
│   └── TaxRateControllerTest.java
├── product/
│   ├── ProductServiceImplTest.java
│   └── ProductControllerTest.java
├── stock/
│   ├── StockServiceImplTest.java
│   └── StockControllerTest.java
├── orderItem/
│   ├── OrderItemServiceImplTest.java
│   └── OrderItemControllerTest.java
└── bill/
    ├── BillServiceImplTest.java
    └── BillControllerTest.java
```

## Configuration

### API Version Prefix

The API version is configured via `server.servlet.context-path` in `application.properties`:

```properties
server.servlet.context-path=/api/v1
```

All endpoints automatically receive the `/api/v1` prefix. The context path is stripped before passing to Spring Security, so the security matcher still uses `/auth/**`.

### Test Configuration

```properties
# src/test/resources/application.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
jwt.secret=VGhpc0lzQVNlY3VyZUtleUZvckpXVFRoYXRNZWV0czI1NkJpdHMhIQ==
jwt.expirationSeconds=8640000
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
        Role role,
        boolean active
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
| `UserImpl` | User authentication | id, name, lastname, email, password, role, active |
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
@RequestMapping("/users")
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

**Note:** The `/api/v1` prefix is added via `server.servlet.context-path` in `application.properties`, not in the controller.

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

**Exception Mappings:**

| Exception | HTTP Status | Message |
|-----------|-------------|---------|
| `UnprocessableEntityException` | 422 | Exception message |
| `BadCredentialsException` | 401 | "Invalid credentials" |
| `IllegalArgumentException` | 409 | Exception message |
| `UsernameNotFoundException` | 404 | Exception message |
| `EntityNotFoundException` | 404 | Exception message |
| `ResourceNotFoundException` | 404 | Exception message |
| `MethodArgumentNotValidException` | 400 | Field-specific validation errors |
| `DataIntegrityViolationException` | 409 | "A record with this value already exists" |

### Security

- JWT-based authentication
- Stateless sessions (`SessionCreationPolicy.STATELESS`)
- Public endpoints under `/auth/**`
- All other endpoints require authentication
- Passwords encrypted with BCrypt
- Use `@EnableMethodSecurity` for method-level role checks
- Use `@PreAuthorize("hasRole('ADMIN')")` for role-based access control

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig { ... }
```

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ResponseEntity<UserResponseDto> create(...) { ... }

@PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
@PatchMapping("/{id}/password")
public ResponseEntity<Void> changePassword(...) { ... }
```

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
- `GET /users` - Get all users (ADMIN only)
- `GET /users/{id}` - Get user by ID (ADMIN only)
- `GET /users/me` - Get current user profile
- `POST /users` - Create user (ADMIN only)
- `PUT /users/{id}` - Update user (ADMIN only)
- `DELETE /users/{id}` - Soft delete user (ADMIN only)
- `PATCH /users/{id}/password` - Change password (USER_OWNS or ADMIN)

### Categories (requires JWT)
- `GET /categories` - Get all categories
- `GET /categories/{id}` - Get category by ID

### Products (requires JWT)
- `GET /products` - Get all products
- `GET /products/{id}` - Get product by ID
- `POST /products` - Create product (ADMIN only)
- `PUT /products/{id}` - Update product (ADMIN only)
- `DELETE /products/{id}` - Soft delete product (ADMIN only)

### Stocks (requires JWT, ADMIN only)
- `GET /stocks/product/{productId}` - Get stock by product ID
- `GET /stocks/low-stock` - Get all low stock items
- `PATCH /stocks/product/{productId}/low-threshold` - Update low stock threshold
- `PATCH /stocks/product/{productId}/refill` - Refill stock quantity

### Tables (requires JWT)
- `GET /tables` - Get all tables
- `GET /tables/{id}` - Get table by ID

### OrderItems (requires JWT)
- `POST /order-items` - Create order item (ADMIN or WAITER)
- `GET /order-items/tab/{tabId}` - Get order items by tab ID
- `DELETE /order-items/{id}` - Delete order item (ADMIN or WAITER)

### Tabs (requires JWT)
- `GET /tabs` - Get all tabs
- `GET /tabs/{id}` - Get tab by ID

### Bills (requires JWT)
- `POST /bills` - Create bill (CASHIER or ADMIN)
- `GET /bills` - Get all bills
- `GET /bills/{id}` - Get bill by ID
- `PATCH /bills/{id}/cancel` - Cancel bill (CASHIER or ADMIN)

## Database

- PostgreSQL on localhost:5432
- Database name: barbilling
- Username: postgres / Password: postgres
- Hibernate DDL: update (not create-drop in production)
