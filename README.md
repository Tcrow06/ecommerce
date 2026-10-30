# E-commerce Backend

Backend system for an E-commerce platform, built with **Java 21** and **Spring Boot**.

The project aims to provide a modular, maintainable, and scalable backend architecture for an online shopping platform. It is designed to support core e-commerce workflows such as user authentication, product catalog management, shopping carts, order processing, inventory management, and payment integration.

> **Project status:** Initial development — Backend only. Frontend has not been developed yet.

---

## 1. Project Overview

This project is an E-commerce backend application designed to handle the core business operations of an online shopping platform.

The initial development focuses on establishing a clean project structure, defining domain boundaries, and implementing backend business logic with clear separation of responsibilities.

### Main objectives

- Build a maintainable backend using Java and Spring Boot.
- Organize the application around business domains.
- Separate API, application, domain, and infrastructure responsibilities.
- Design reliable order, inventory, and payment workflows.
- Apply transaction management and concurrency control where required.
- Provide RESTful APIs for future frontend integration.
- Prepare the architecture for future scaling and extension.

## 2. Technology Stack

| Category             | Technology                  |
| -------------------- | --------------------------- |
| Programming Language | Java 21                     |
| Backend Framework    | Spring Boot 4.1.2           |
| Build Tool           | Gradle                      |
| ORM                  | Spring Data JPA / Hibernate |
| Database             | PostgreSQL                  |
| API Style            | RESTful API                 |
| Code Generation      | Lombok                      |
| Development Tools    | Spring Boot DevTools        |
| Testing              | JUnit 5, Spring Boot Test   |

### Gradle dependencies

The project currently uses the following dependencies:

- `spring-boot-starter-data-jpa` — Database persistence and ORM.
- `spring-boot-starter-webmvc` — REST API and HTTP request handling.
- `lombok` — Reduce boilerplate code.
- `spring-boot-devtools` — Development productivity.
- `postgresql` — PostgreSQL JDBC driver.
- Spring Boot test dependencies — Application and persistence testing.

### Java and Spring configuration

- Java toolchain: 21
- Spring Boot: 4.1.2
- Spring Dependency Management: 1.1.7
- Group: `vn.io.thinz`
- Version: `0.0.1-SNAPSHOT`

## 3. Architecture

The project follows a **Modular Monolith** architecture, incorporating selected principles from Clean Architecture and Domain-Driven Design (DDD).

The application is initially developed and deployed as a single Spring Boot application, while business domains are separated into independent modules.

### Architectural principles

- Domain-oriented modularization.
- Separation of concerns.
- Dependency inversion.
- Explicit application use cases.
- Clear ownership of business data.
- Minimized coupling between modules.
- Infrastructure details isolated from business logic.

### Layer responsibilities

Each business module can be organized into the following layers:

| Layer            | Responsibility                                         |
| ---------------- | ------------------------------------------------------ |
| `api`            | REST controllers, request/response DTOs                |
| `application`    | Use cases, commands, queries, orchestration            |
| `domain`         | Business models, entities, value objects, domain rules |
| `infrastructure` | Database persistence and external integrations         |

## 4. Project Structure

The proposed source structure is organized by business domain:

```text
src/
├── main/
│   ├── java/
│   │   └── vn/io/thinz/
│   │       ├── EcommerceApplication.java
│   │       │
│   │       ├── shared/
│   │       ├── platform/
│   │       │
│   │       ├── identity/
│   │       ├── customer/
│   │       ├── catalog/
│   │       ├── cart/
│   │       ├── checkout/
│   │       ├── order/
│   │       ├── inventory/
│   │       ├── payment/
│   │       ├── shipping/
│   │       └── notification/
│   │
│   └── resources/
│       ├── application.yml
│       └── db/
│           └── migration/
│
└── test/
    └── java/
        └── vn/io/thinz/
```

The structure above is the intended architecture. Modules and packages will be introduced progressively as their business requirements are implemented.

### Core modules

| Module         | Description                                                       |
| -------------- | ----------------------------------------------------------------- |
| `identity`     | User accounts, authentication, credentials, roles and permissions |
| `customer`     | Customer profiles and saved addresses                             |
| `catalog`      | Products, categories, variants and product information            |
| `cart`         | Shopping carts and cart items                                     |
| `checkout`     | Checkout orchestration and purchase validation                    |
| `order`        | Order creation, order details and order lifecycle                 |
| `inventory`    | Stock management and inventory reservation                        |
| `payment`      | Payment processing, payment attempts, callbacks and refunds       |
| `shipping`     | Shipment management and delivery tracking                         |
| `notification` | Email and other notification channels                             |

## 5. Core Business Workflows

The system is intended to support the following workflows:

### Product Management

- Manage products and categories.
- Manage product variants and SKU information.
- Retrieve product details and product listings.

### Shopping Cart

- Add products to cart.
- Update item quantities.
- Remove cart items.
- Retrieve cart information.

### Checkout and Order Processing

- Validate cart items and product availability.
- Recalculate prices on the backend.
- Reserve inventory.
- Create orders.
- Track order status.

### Inventory Management

- Track available stock.
- Reserve stock during checkout.
- Release reservations when appropriate.
- Prevent overselling through concurrency control.

### Payment Processing

- Create payment attempts.
- Integrate with external payment providers.
- Process payment callbacks/webhooks.
- Handle payment failures and retries.
- Support refund workflows when required.

These workflows describe the intended business scope; implementation and supported behavior will evolve during development.

## 6. Database

**PostgreSQL** is the primary relational database.

Spring Data JPA and Hibernate are used for persistence.

Database design will focus on:

- Clear ownership of data by business domain.
- Appropriate primary keys and foreign keys.
- Database constraints for business invariants.
- Indexing for frequently queried data.
- Transaction boundaries.
- Safe handling of concurrent updates.
- Version-controlled database schema changes.

## 7. API Design

The backend exposes RESTful APIs for future frontend and external client integration.

Proposed API prefix:

```text
/api/v1
```

Example endpoints (planned):

```http
GET    /api/v1/products
GET    /api/v1/products/{productId}

POST   /api/v1/cart/items
PATCH  /api/v1/cart/items/{itemId}
DELETE /api/v1/cart/items/{itemId}

POST   /api/v1/orders
GET    /api/v1/orders/{orderId}
POST   /api/v1/orders/{orderId}/cancel
```

Actual endpoints and request/response contracts will be defined as the corresponding modules are implemented.

## 8. Getting Started

### Prerequisites

Make sure the following tools are installed:

- Java Development Kit 21
- Gradle (or use the included Gradle Wrapper)
- PostgreSQL

### Clone the repository

```bash
git clone <repository-url>
cd ecommerce-backend
```

### Configure the database

Create a PostgreSQL database for the application.

Configure the database connection in `application.yml` or through environment variables.

Example configuration:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/ecommerce}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

> This is an example configuration. Database migration and schema initialization must be configured before using `ddl-auto: validate`.

Do not commit production credentials or secrets to the repository.

### Run the application

On Linux/macOS:

```bash
./gradlew bootRun
```

On Windows:

```bash
gradlew.bat bootRun
```

### Run tests

```bash
./gradlew test
```

### Build the application

```bash
./gradlew clean build
```

## 9. Development Guidelines

The following principles guide future development:

1. Keep business logic inside the appropriate domain/application layers.
2. Avoid placing business logic inside REST controllers.
3. Do not expose persistence entities directly through API responses.
4. Avoid direct access to another module's internal repositories.
5. Define explicit use cases for important business operations.
6. Use database constraints to protect critical business invariants.
7. Handle concurrent inventory updates safely.
8. Make payment callbacks and retryable operations idempotent.
9. Keep external integrations behind appropriate interfaces/adapters.
10. Add tests for important business rules and transaction behavior.

## 10. Frontend Integration

The project currently contains **Backend only**.

A separate frontend application may be developed in the future and integrated through the REST API.

Proposed repository organization:

```text
ecommerce-backend/
    Java + Spring Boot

ecommerce-frontend/
    Frontend application (technology to be decided)
```

Backend and frontend will maintain independent development and deployment pipelines while sharing API contracts.

## 11. Roadmap

The implementation will be developed incrementally.

- [ ] Project foundation and application configuration
- [ ] Database configuration and schema migration
- [ ] Identity and authentication
- [ ] Customer profile management
- [ ] Product catalog
- [ ] Shopping cart
- [ ] Inventory management
- [ ] Checkout and order processing
- [ ] Payment integration
- [ ] Shipping management
- [ ] Notification system
- [ ] Integration and concurrency testing
- [ ] Docker and deployment configuration
- [ ] Frontend integration

The roadmap is a planning baseline, not a claim that these features are already implemented.

---

## License

To be determined.
