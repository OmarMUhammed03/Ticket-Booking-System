# Distributed Ticket Booking System
![Java](https://img.shields.io/badge/Java-21-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=spring-boot)
![Docker](https://img.shields.io/badge/Docker-28.1.1-blue?logo=docker)
![Kafka](https://img.shields.io/badge/Kafka-2.12--2.2.1-orange?logo=apache-kafka)
![CI/CD](https://img.shields.io/badge/CI%2FCD-passing-brightgreen?logo=github-actions)
![License](https://img.shields.io/badge/License-MIT-yellow?logo=open-source-initiative)
![Swagger](https://img.shields.io/badge/Swagger-3.0-85EA2D?logo=swagger)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?logo=json-web-tokens)

## Badges

## Overview

This project is a resilient, scalable, and distributed platform for booking event tickets, engineered from the ground up to handle high-traffic scenarios and ensure data consistency in a complex, multi-service environment.

The system is architected using a **Microservices approach** to ensure separation of concerns, independent scalability, and maintainability. The core of the system is built on the principles of **Event-Driven Design**, leveraging Apache Kafka to facilitate asynchronous communication and orchestrate complex business transactions that span multiple services.

## Core Features & Architecture

The system is composed of several key microservices that work together, supported by a shared common library to reduce code duplication and enforce standards.

- **API Gateway**: Serves as the single entry point for all client requests. It routes incoming HTTP requests to the appropriate microservice, handles authentication and authorization (using JWT), and can perform request/response transformations. The API Gateway centralizes cross-cutting concerns improving security by hiding internal service endpoints.

- **Event Service**: The source of truth for the event catalog. Manages the creation, details, and inventory of events, venues, and ticket configurations.

- **Booking Service**: The transactional engine that manages the entire booking lifecycle using the **Saga Choreography Pattern**. It orchestrates the reservation, payment, and confirmation steps through events.

- **Payment Service**: A decoupled service responsible for processing payments by listening for events and integrating with external payment gateways.

- **User Service**: Manages user profiles, registration, and user-related data. Handles user CRUD operations and provides user information to other services as needed.

- **Auth Service**: Responsible for authentication and authorization. Issues and validates JWT tokens, manages login, registration, and user roles, and integrates with the API Gateway for secure access control.
- **Distributed Caching with Redis**: To handle high-concurrency scenarios and prevent race conditions, the system uses a distributed cache (Redis) for temporary ticket reservations. When a user initiates a booking, the ticket is atomically checked and temporarily locked in the cache. This lock has a configured expiration time, ensuring that if the booking is not completed within a certain window (e.g., due to payment failure or user abandonment), the ticket is automatically released and made available to other users.
- **`common-library`**: A shared Maven module that centralizes common logic and configuration across all microservices. This library includes:

    - **Global Exception Handling:** A centralized `@RestControllerAdvice` to ensure consistent error responses across all APIs.
    - **Kafka Configuration:** Reusable beans for Kafka `ProducerFactory`, `ConsumerFactory`, and `KafkaTemplate`.
    - **Kafka Producer Service:** A generic producer class that can be injected into any service to publish events without boilerplate code.

<details>
<summary><strong>API Gateway Logic</strong></summary>

The API Gateway is implemented as a Spring Boot application. Its main responsibilities include:

- **Routing:** Forwards requests to the correct microservice based on the URL path (e.g., `/events/**` to the Event Service, `/bookings/**` to the Booking Service, etc.).
- **Authentication & Authorization:** Validates JWT tokens on protected routes and enforces role-based access control.
- **CORS & Security:** Applies CORS policies and other security headers to all incoming requests.
- **Aggregation (optional):** Can aggregate responses from multiple services for composite endpoints. For example, API composition is used in the gateway to fetch a user's booking history by combining data from multiple services.
- **Error Handling:** Returns standardized error responses for failed requests.

Example configuration (application.yml):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: event-service
          uri: http://localhost:8081
          predicates:
            - Path=/events/**
        - id: booking-service
          uri: http://localhost:8082
          predicates:
            - Path=/bookings/**
        - id: payment-service
          uri: http://localhost:8083
          predicates:
            - Path=/payments/**
```

JWT authentication is enforced using a filter that checks the `Authorization` header and validates the token before forwarding the request.

</details>

<details>
<summary><strong>Code Style & Quality</strong></summary>

The project repositories adhere to Java conventions and best practices for consistency and maintainability:

### Naming Conventions:

- **Classes**: PascalCase (e.g., `EventService`, `BookingController`)
- **Methods and Variables**: camelCase (e.g., `reserveTicket`, `bookingId`)
- **Constants**: UPPER_SNAKE_CASE
- **Packages**: Lowercase with meaningful hierarchy (e.g., `org.example.eventservice.service`)

### Best Practices:

- Descriptive names that reflect functionality.
- Single-responsibility principle for classes and methods.
- JavaDoc for public APIs.
- **Automated Style Enforcement**: Code style and formatting are automatically verified on every pull request using **Maven Checkstyle**, ensuring consistency across the codebase.

</details>

<details>
<summary><strong>Code Snippets</strong></summary>

**Booking Service: Initiating the Saga**

```java
private final CacheManager cacheManager;
private final RestTemplate restTemplate;
public BookingResponseDto createBooking(final BookingRequestDto bookingRequestDto, final UUID userId,
                                        final String authHeader) {
    if (bookingRequestDto.getEventId() == null || bookingRequestDto.getTicketId() == null) {
        throw new ValidationException("Event ID and Ticket ID must not be null");
    }
    String ticketAvailableUrl = GATEWAY_URL + bookingRequestDto.getEventId() + "/tickets/"
            + bookingRequestDto.getTicketId() + "/available";
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
    HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<Boolean> response = restTemplate.exchange(ticketAvailableUrl,
            HttpMethod.GET,
            requestEntity,
            Boolean.class);
    if (Boolean.FALSE.equals(response.getBody())) {
        throw new ValidationException("Ticket is not available");
    }
    Cache ticketReservationCache = cacheManager.getCache("ticketReservations");
    if (ticketReservationCache != null && ticketReservationCache.get(bookingRequestDto.getTicketId()) != null) {
        throw new ValidationException("Ticket is already reserved, please try again later");
    }
    bookingRequestDto.setBookingDate(LocalDateTime.now());
    bookingRequestDto.setBookingStatus(String.valueOf(PENDING));
    Booking booking = BookingMapper.toBooking(bookingRequestDto, userId);
    Booking created = bookingRepository.save(booking);
    messageProducer.sendMessage("reserve-ticket", "ticket-available", new HashMap<>(
            Map.of("ticketId", bookingRequestDto.getTicketId(), "eventId", bookingRequestDto.getEventId(),
                    "userId", userId, "bookingId", created.getId())
    ));
    ticketReservationCache.put(bookingRequestDto.getTicketId(), true);
    return BookingMapper.toBookingResponseDto(created);
}
```

**common-library: Global Exception Handler**

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex,
            @NonNull final HttpHeaders headers,
            @NonNull final HttpStatusCode status,
            @NonNull final WebRequest request) {

        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField()).append(": ")
                    .append(error.getDefaultMessage()).append("; ");
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage.toString(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(final NotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidActionException(final InvalidActionException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    // ....
}

```

</details>

## Continuous Integration (CI)

This project utilizes a robust CI pipeline powered by **GitHub Actions** to ensure code quality and stability. The workflow is defined in `.github/workflows/ci.yml`.

- **Monorepo Change Detection:** The pipeline intelligently detects which microservices have changed in a push or pull request, running builds and tests only for the affected services.
- **Parallel Matrix Builds:** Each affected service is built and tested in parallel, significantly reducing CI run times.
- **Automated Quality Gates:** The pipeline automatically enforces:
    1.  **Code Style** via Maven Checkstyle.
    2.  **Builds & Tests** via `mvn verify`, which runs the full suite of unit and integration tests.
- **Integration Testing with Docker:** Before running tests, the CI pipeline spins up the necessary infrastructure (Kafka, PostgreSQL) using Docker Compose, allowing integration tests to run against real dependencies.

## Testing Strategy

Each microservice contains a dedicated and comprehensive test suite to ensure its reliability and correctness.

- **Unit Tests:** Written with **JUnit 5 & Mockito**, these tests verify the logic of individual classes in complete isolation from the database or other external components.
- **Integration Tests:** These tests validate the interactions between different layers of a service (Controller -> Service -> Repository).
All tests are automatically executed in the CI pipeline for every pull request to the `dev` branch. To run tests locally for a service:

```bash
mvn verify
```

## Frameworks and Technologies Used

- [Java 21](https://www.oracle.com/java/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Apache Kafka](https://kafka.apache.org/)
- [PostgreSQL](https://www.postgresql.org/)
- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- [Maven](https://maven.apache.org/)
- [JUnit 5](https://junit.org/junit5/) & [Mockito](https://site.mockito.org/)
- [GitHub Actions](https://github.com/features/actions) for CI
- [Swagger/OpenAPI](https://swagger.io/) for API documentation
- [JWT](https://jwt.io/) for secure authentication

## API Documentation

The API documentation is available through Swagger UI at `/swagger-ui/index.html` when running any service, don't forget to add /api before the route because it is the service context.

## Authentication

The system uses JWT (JSON Web Tokens) for secure authentication:

- Tokens are required for all protected endpoints
- Include the JWT token in the Authorization header: `Authorization: Bearer <token>`
- Tokens expire after 24 hours and must be renewed
- Role-based access control (ADMIN, USER) is enforced for sensitive operations

## Installation & Usage

To get started with the platform, you need to have Java 21, Maven, and Docker installed.

1.  **Start Infrastructure:** The entire backend environment is containerized. Start it with a single command from the root directory:

    ```bash
    docker-compose up --build
    ```

2.  **Build & Run Microservices:** Each microservice is a standard Spring Boot application. To run a service (e.g., the `event-service`):

    ```bash
    # Navigate to the service's directory
    cd event-service

    # Build the application using Maven (this also installs the common-library)
    mvn clean install

    # Run the application
    java -jar target/event-service-*.jar
    ```

    Repeat this process for the `booking-service` and any other services.

## Credits & Contributions

- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Docs](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/documentation)
- [Docker Docs](https://docs.docker.com/)
- [Docker Compose Docs](https://docs.docker.com/compose/)
- [Maven Documentation](https://maven.apache.org/guides/index.html)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Swagger/OpenAPI Docs](https://swagger.io/docs/)
- [JWT Introduction](https://jwt.io/introduction)

### How to Contribute

We welcome contributions from the community! To contribute:

1. Fork the repository and create your branch from `dev`.
2. Make your changes, following the code style and best practices outlined above.
3. Ensure all tests pass and code is properly formatted.
4. Submit a pull request with a clear description of your changes.
