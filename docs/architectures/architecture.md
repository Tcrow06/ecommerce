
ecommerce/
├── pom.xml
├── docker-compose.yml
├── Dockerfile
├── README.md
│
├── docs/
│   ├── architecture/
│   │   ├── domain-boundaries.md
│   │   ├── payment-flow.md
│   │   ├── order-flow.md
│   │   └── kafka-event-flow.md
│   ├── decisions/
│   │   ├── ADR-001-modular-monolith.md
│   │   ├── ADR-002-outbox-pattern.md
│   │   └── ADR-003-payment-idempotency.md
│   └── diagrams/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/ecommerce/
│   │       ├── EcommerceApplication.java
│   │    
│   │       ├── shared/
│   │       │   ├── domain/
│   │       │   │   ├── Money.java
│   │       │   │   └── DomainEvent.java
│   │       │   ├── exception/
│   │       └── util/
│   │    
│   │       ├── platform/
│   │       │   ├── config/
│   │       │   ├── security/
│   │       │   ├── messaging/
│   │       │   ├── outbox/
│   │       │   ├── idempotency/
│   │       │   ├── observability/
│   │       │   └── persistence/
│   │       │
│   │       ├── identity/
│   │       ├── catalog/
│   │       ├── cart/
│   │       ├── checkout/
│   │       ├── order/
│   │       ├── inventory/
│   │       ├── payment/
│   │       ├── shipping/
│   │       └── notification/
│   │
│   └── test/
│       └── java/com/example/ecommerce/
│           ├── catalog/
│           ├── order/
│           ├── inventory/
│           ├── payment/
│           ├── integration/
│           └── architecture/
│
└── src/main/resources/
    ├── application.yml
    ├── application-local.yml
    ├── application-test.yml
    └── db/migration/
        ├── V1__create_catalog.sql
        ├── V2__create_inventory.sql
        ├── V3__create_orders.sql
        ├── V4__create_payments.sql
        └── V5__create_outbox.sql

identity/
├── api/
│   ├── AuthController.java
│   └── dto/
│       ├── RegisterRequest.java
│       ├── LoginRequest.java
│       └── AuthResponse.java
│
├── application/
│   ├── usecase/
│   │   ├── RegisterUserUseCase.java
│   │   └── LoginUseCase.java
│   │
│   └── port/
│       ├── UserRepository.java
│       ├── PasswordHasher.java
│       └── TokenService.java
│
├── domain/
│   ├── model/
│   │   └── User.java
│   └── exception/
│       └── UserAlreadyExistsException.java
│
└── infrastructure/
    ├── persistence/
    │   ├── entity/
    │   │   └── UserEntity.java
    │   ├── repository/
    │   │   └── JpaUserRepository.java
    │   └── mapper/
    │       └── UserPersistenceMapper.java
    │
    └── security/
        ├── config/
        │   └── SecurityConfig.java
        │
        ├── jwt/
        │   ├── JwtTokenService.java
        │   ├── JwtAuthenticationFilter.java
        │   └── JwtProperties.java
        │
        ├── password/
        │   └── BCryptPasswordHasher.java
        │
        └── authentication/
            └── CustomUserDetailsService.java
    


payment/
├── api/
│   ├── PaymentController.java
│   ├── PaymentWebhookController.java
│   ├── dto/
│   │   ├── CreatePaymentRequest.java
│   │   └── PaymentResponse.java
│   └── mapper/
│
├── application/
│   ├── usecase/
│   │   ├── CreatePaymentUseCase.java
│   │   ├── HandlePaymentReturnUseCase.java
│   │   ├── HandlePaymentWebhookUseCase.java
│   │   ├── RetryPaymentUseCase.java
│   │   └── RefundPaymentUseCase.java
│   ├── port/
│   │   ├── PaymentRepository.java
│   │   ├── PaymentGateway.java
│   │   └── PaymentEventPublisher.java
│   └── service/
│
├── domain/
│   ├── model/
│   │   ├── Payment.java
│   │   ├── PaymentAttempt.java
│   │   └── Refund.java
│   ├── enums/
│   │   ├── PaymentStatus.java
│   │   └── RefundStatus.java
│   ├── event/
│   │   ├── PaymentSucceeded.java
│   │   └── PaymentFailed.java
│   ├── service/
│   └── exception/
│
└── infrastructure/
    ├── persistence/
    │   ├── JpaPaymentRepository.java
    │   ├── PaymentJpaRepository.java
    │   └── entity/
    ├── vnpay/
    │   ├── VnPayGatewayAdapter.java
    │   ├── VnPayRequestFactory.java
    │   ├── VnPaySignatureVerifier.java
    │   └── VnPayProperties.java
    └── messaging/
        ├── PaymentEventKafkaPublisher.java
        └── PaymentEventConsumer.java

