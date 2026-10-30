# Đặc tả kiến trúc Backend E-commerce

- **Phiên bản:** 1.0
- **Ngày:** 2026-10-30
- **Trạng thái:** Đề xuất thiết kế
- **Nền tảng tham chiếu:** Java 21+, Spring Boot 3.x, PostgreSQL

> Tài liệu này mô tả kiến trúc đề xuất cho backend thương mại điện tử. Đây là bản đặc tả nền tảng để bắt đầu thiết kế và triển khai, không phải khẳng định về một hệ thống đã tồn tại. Các quyết định về payment provider, chính sách tồn kho, thuế, vận chuyển và quy mô tải cần được xác nhận theo yêu cầu sản phẩm.

---

## 1. Mục tiêu

Thiết kế backend có cấu trúc rõ ràng theo domain nghiệp vụ, dễ kiểm thử, bảo trì và mở rộng.

Các mục tiêu chính:

1. Phân tách trách nhiệm giữa API, application, domain và infrastructure.
2. Mỗi domain sở hữu nghiệp vụ và dữ liệu của mình.
3. Hạn chế phụ thuộc chéo và tránh domain gọi trực tiếp repository nội bộ của domain khác.
4. Xử lý an toàn các luồng quan trọng: checkout, order, inventory và payment.
5. Có phương án cho retry, idempotency, concurrency, timeout và lỗi từ hệ thống ngoài.
6. Có thể bắt đầu dưới dạng một ứng dụng Spring Boot duy nhất, chưa cần triển khai microservices.

## 2. Phạm vi nghiệp vụ

Các module dự kiến:

| Module | Trách nhiệm |
|---|---|
| `identity` | Tài khoản, thông tin xác thực, role/permission, đăng nhập |
| `customer` | Hồ sơ khách hàng và sổ địa chỉ đã lưu (nếu tách riêng) |
| `catalog` | Sản phẩm, danh mục, biến thể/SKU, thuộc tính |
| `cart` | Giỏ hàng và các sản phẩm người dùng đang chọn |
| `checkout` | Điều phối quy trình xác nhận mua hàng |
| `order` | Đơn hàng, chi tiết đơn hàng và vòng đời đơn |
| `inventory` | Tồn kho, giữ hàng (reservation), giải phóng/tiêu thụ tồn kho |
| `payment` | Payment, payment attempt, webhook, refund |
| `shipping` | Shipment, đơn vị vận chuyển, tracking |
| `notification` | Email, SMS, push notification và lịch sử gửi |

Danh sách này là điểm khởi đầu. Có thể gộp hoặc tách module tùy phạm vi sản phẩm. Không nên tạo module riêng chỉ vì có một entity riêng.

## 3. Kiến trúc tổng thể

### 3.1. Kiến trúc được đề xuất

Bắt đầu với **Modular Monolith**, kết hợp các nguyên tắc chọn lọc từ **Clean Architecture** và **Domain-Driven Design**.

Modular Monolith có nghĩa là ứng dụng có thể build và deploy như một đơn vị, nhưng source code được chia thành các module nghiệp vụ có ranh giới rõ ràng.

Lý do lựa chọn:

- Dễ phát triển, debug và triển khai hơn hệ thống microservices ngay từ đầu.
- Có thể sử dụng transaction database cho các thao tác cần atomic trong cùng phạm vi.
- Giảm chi phí vận hành network, service discovery, distributed tracing và xử lý lỗi liên service.
- Vẫn có thể xem xét tách module thành service riêng khi có nhu cầu thực tế và ranh giới domain đã ổn định.

Không nên hiểu Modular Monolith là bước chuyển đổi sang microservices tự động. Tách service sau này vẫn cần thiết kế lại data ownership, giao tiếp và consistency.

### 3.2. Sơ đồ phụ thuộc

```text
Client
  |
  v
API Layer
  |  Controller, Request/Response DTO
  v
Application Layer
  |  Use case, command/query, orchestration, ports
  v
Domain Layer
  |  Entity, Value Object, business rules, domain events

Infrastructure Layer
  |  JPA, database, payment gateway, message broker,
  |  email provider, object storage
  +---- implements ports required by application
```

### 3.3. Quy tắc phụ thuộc

1. `api` gọi use case ở `application`.
2. `application` điều phối nghiệp vụ và phụ thuộc vào abstraction/port cần thiết.
3. `domain` không phụ thuộc Spring MVC, Spring Data, JPA repository hoặc SDK của nhà cung cấp bên ngoài.
4. `infrastructure` triển khai các port và chứa chi tiết kỹ thuật.
5. Module này không truy cập trực tiếp repository nội bộ của module khác.
6. Không chia sẻ JPA entity giữa các domain.
7. Không tạo dependency vòng giữa các module.
8. Chỉ dùng event khi phù hợp với consistency và yêu cầu xử lý bất đồng bộ; không dùng event thay thế mọi lời gọi đồng bộ.

## 4. Cấu trúc thư mục cấp cao

```text
src/main/java/com/example/ecommerce/
├── EcommerceApplication.java
├── shared/
│   ├── domain/
│   ├── application/
│   └── api/
├── platform/
│   ├── config/
│   ├── security/
│   ├── observability/
│   └── web/
├── identity/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── customer/
├── catalog/
├── cart/
├── checkout/
├── order/
├── inventory/
├── payment/
├── shipping/
└── notification/
```

Không bắt buộc tạo mọi module hoặc package rỗng ngay từ đầu. Chỉ thêm cấu trúc khi module có nghiệp vụ thực tế.

### 4.1. `shared/`

Chứa abstraction hoặc kiểu dữ liệu thực sự dùng chung và ổn định.

Ví dụ có thể cân nhắc:

- `Money`, nếu các domain dùng chung cùng semantics về tiền.
- `PageResponse`, nếu contract phân trang được thống nhất.
- Một số kiểu lỗi hoặc abstraction kỹ thuật dùng chung.

Không biến `shared` thành nơi chứa tất cả class chưa biết đặt ở đâu. Nếu một class chỉ phục vụ một domain, hãy đặt nó trong domain đó.

### 4.2. `platform/`

Chứa cấu hình và cross-cutting technical capabilities:

- Spring Security configuration.
- Cấu hình Jackson, OpenAPI.
- Exception handler ở tầng web.
- Logging, metrics, tracing.
- Clock/configuration.
- Cấu hình message broker và các adapter nền tảng.

`platform` không nên chứa nghiệp vụ cụ thể của order, payment hoặc inventory.

## 5. Cấu trúc bên trong mỗi domain

Ví dụ:

```text
order/
├── api/
│   ├── OrderController.java
│   ├── request/
│   ├── response/
│   └── mapper/
├── application/
│   ├── command/
│   ├── query/
│   ├── service/
│   └── port/
├── domain/
│   ├── model/
│   ├── event/
│   ├── exception/
│   └── repository/
└── infrastructure/
    └── persistence/
        ├── entity/
        ├── springdata/
        ├── mapper/
        └── adapter/
```

### 5.1. `api/` — giao tiếp bên ngoài

Trách nhiệm:

- Nhận HTTP request.
- Validate dữ liệu đầu vào ở mức cấu trúc/định dạng.
- Chuyển request DTO thành command/query.
- Gọi application use case.
- Chuyển kết quả thành response DTO và HTTP status.

Ví dụ:

```text
api/
├── OrderController.java
├── request/
│   └── PlaceOrderRequest.java
├── response/
│   └── OrderResponse.java
└── mapper/
    └── OrderApiMapper.java
```

Không đặt business rules trong controller. Controller không nên gọi JPA repository trực tiếp hoặc tự gọi payment provider.

### 5.2. `application/` — điều phối use case

Chứa các hành động mà hệ thống cung cấp:

- `PlaceOrderUseCase`
- `CancelOrderUseCase`
- `GetOrderDetailUseCase`
- `ReserveInventoryUseCase`
- `CreatePaymentUseCase`

Có thể có các thư mục:

| Package | Trách nhiệm |
|---|---|
| `command/` | Dữ liệu đầu vào cho thao tác thay đổi trạng thái |
| `query/` | Dữ liệu đầu vào cho thao tác đọc |
| `service/` | Điều phối use case |
| `port/` | Interface để application giao tiếp với persistence/provider bên ngoài |

Không cần tạo interface cho mọi service một cách máy móc. Interface có giá trị khi nó thể hiện một boundary hoặc abstraction có lý do rõ ràng.

### 5.3. `domain/` — mô hình nghiệp vụ

Chứa:

- Entity/Aggregate.
- Value Object.
- Enum trạng thái.
- Business rules và invariant.
- Domain service khi quy tắc không thuộc tự nhiên một entity.
- Domain event.
- Domain exception.
- Repository contract nếu domain cần định nghĩa abstraction đó.

Ví dụ:

```text
domain/
├── model/
│   ├── Order.java
│   ├── OrderLine.java
│   └── OrderStatus.java
├── event/
│   └── OrderPlacedEvent.java
├── exception/
│   └── OrderCannotBeCancelledException.java
└── repository/
    └── OrderRepository.java
```

Domain model không nên chứa logic phụ thuộc HTTP hoặc gọi trực tiếp SDK payment provider.

### 5.4. `infrastructure/` — triển khai kỹ thuật

Chứa chi tiết kỹ thuật:

- JPA entity.
- Spring Data repository.
- Adapter triển khai repository/port.
- Tích hợp payment gateway.
- Email/SMS provider.
- Message broker.
- Object storage.

Ví dụ:

```text
infrastructure/
├── persistence/
│   ├── entity/
│   │   └── OrderJpaEntity.java
│   ├── springdata/
│   │   └── SpringDataOrderRepository.java
│   ├── mapper/
│   │   └── OrderPersistenceMapper.java
│   └── adapter/
│       └── OrderRepositoryAdapter.java
└── payment/
```

## 6. Phân biệt các loại model

| Loại model | Ví dụ | Mục đích |
|---|---|---|
| API Request DTO | `PlaceOrderRequest` | Định dạng input HTTP |
| Application Command | `PlaceOrderCommand` | Ý định thực hiện use case |
| Domain Model | `Order`, `OrderLine` | Trạng thái và quy tắc nghiệp vụ |
| Persistence Entity | `OrderJpaEntity` | Mapping database/ORM |
| API Response DTO | `OrderResponse` | Định dạng output HTTP |

Không bắt buộc mọi use case đều phải có đủ các loại trên. Có thể dùng chung model khi đơn giản và không tạo coupling đáng kể. Nên tách khi mỗi model có lý do thay đổi khác nhau hoặc khi ORM/API làm ảnh hưởng business model.

### 6.1. Quy ước đặt tên

- Request: `CreateProductRequest`, `PlaceOrderRequest`.
- Response: `ProductResponse`, `OrderDetailResponse`.
- Command: `PlaceOrderCommand`, `CancelOrderCommand`.
- Result: `PlaceOrderResult`.
- Use case: `PlaceOrderUseCase`, `CancelOrderUseCase`.
- Domain model: `Order`, `OrderLine`.
- Persistence: `OrderJpaEntity`.
- Adapter: `OrderRepositoryAdapter`.
- Port: `PaymentGateway`, `InventoryReservationPort`.

## 7. Đặc tả ranh giới domain

### 7.1. Identity

Quản lý:

- Account.
- Credential/password hash.
- Authentication.
- Role/permission.
- Refresh token/session nếu hệ thống sử dụng.

Không nên mặc định đưa tất cả thông tin liên quan đến một người dùng vào `identity`.

### 7.2. Customer

Nếu sản phẩm cần hồ sơ khách hàng và sổ địa chỉ, có thể tách module `customer`:

- Customer profile.
- Customer address book.
- Thông tin liên hệ và tùy chọn nhận hàng.

Địa chỉ giao hàng đã dùng trong một order nên được lưu thành snapshot trong order. Nếu người dùng sửa địa chỉ mặc định sau đó, lịch sử order không được thay đổi theo.

### 7.3. Catalog

Quản lý:

- Product.
- Category.
- Product variant/SKU.
- Product attributes.
- Giá niêm yết hoặc thông tin catalog theo chính sách sản phẩm.

Catalog không trực tiếp điều chỉnh tồn kho thực tế nếu inventory là module sở hữu dữ liệu stock.

### 7.4. Cart

Quản lý:

- Cart.
- CartItem.
- Số lượng người dùng chọn.
- Tham chiếu tới product/variant.

Giá hiển thị trong cart chỉ nên được xem là thông tin tham khảo nếu giá có thể thay đổi. Checkout phải xác minh lại giá và tình trạng sản phẩm ở phía server.

### 7.5. Checkout

Checkout điều phối quá trình mua hàng:

- Xác minh cart.
- Xác minh giá và chính sách giảm giá.
- Xác minh địa chỉ và phương thức giao hàng.
- Yêu cầu inventory reservation.
- Tạo order.
- Khởi tạo payment theo quy trình đã chọn.

Checkout không nên trở thành nơi sở hữu toàn bộ dữ liệu order, payment và inventory.

### 7.6. Order

Quản lý:

- Order.
- OrderLine.
- Order status.
- Snapshot sản phẩm/giá/địa chỉ tại thời điểm đặt hàng.
- Lịch sử thay đổi trạng thái nếu yêu cầu audit.

Order là bản ghi lịch sử giao dịch. Không nên phụ thuộc vào giá hiện tại trong catalog để hiển thị lại tổng tiền đơn hàng cũ.

### 7.7. Inventory

Quản lý:

- Stock theo SKU/warehouse nếu có nhiều kho.
- Available quantity.
- Reserved quantity.
- Reservation lifecycle.
- Stock adjustment.

Các module khác phải yêu cầu inventory thực hiện reserve/release/consume thông qua contract công khai, không tự cập nhật bảng stock.

### 7.8. Payment

Quản lý:

- Payment.
- PaymentAttempt.
- Provider reference.
- Payment status.
- Webhook event.
- Refund workflow.

Không lưu dữ liệu thẻ thanh toán nhạy cảm nếu provider đã hỗ trợ tokenization hoặc hosted checkout. Tuân thủ yêu cầu bảo mật của payment provider và phạm vi PCI DSS liên quan.

### 7.9. Shipping

Quản lý:

- Shipment.
- Carrier/provider.
- Tracking code.
- Shipment status/event.
- Tích hợp tạo vận đơn và truy vấn tracking.

### 7.10. Notification

Quản lý:

- Notification request.
- Kênh gửi.
- Delivery attempt.
- Retry và trạng thái gửi.

Notification thường phù hợp với xử lý bất đồng bộ sau khi nghiệp vụ chính đã commit.

## 8. Luồng Checkout — Order — Inventory — Payment

### 8.1. Luồng tham khảo

```text
Client
  |
  | POST /api/v1/orders
  | Idempotency-Key: <unique-key>
  v
Order/Checkout API
  |
  v
PlaceOrderUseCase
  |
  |-- Xác minh customer/cart
  |-- Tính lại giá phía server
  |-- Kiểm tra promotion/shipping
  |-- Reserve inventory
  |-- Tạo order PENDING_PAYMENT
  |-- Tạo payment attempt
  |-- Lưu kết quả idempotency
  |
  v
Trả order và thông tin khởi tạo thanh toán
  |
  v
Payment Provider
  |
  | webhook/callback
  v
Payment module
  |
  |-- Xác minh webhook
  |-- Xử lý idempotent
  |-- Cập nhật payment
  |-- Yêu cầu order transition phù hợp
  |-- Consume/release inventory theo policy
  |
  v
Outbox/Event -> Notification/Fulfillment
```

Đây là luồng khái niệm. Thứ tự chính xác giữa tạo order, reservation và payment cần được chốt dựa trên payment provider và yêu cầu nghiệp vụ.

### 8.2. Order status tham khảo

```text
PENDING_PAYMENT
    |
    | Payment confirmed
    v
CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED
    |
    | Cancellation allowed by policy
    v
CANCELLED
```

Có thể cần các trạng thái như `CANCELLATION_PENDING`, `REFUND_PENDING`, `REFUNDED` hoặc `PAYMENT_REVIEW` tùy nghiệp vụ. Mỗi trạng thái cần định nghĩa transition hợp lệ và điều kiện chuyển.

### 8.3. Payment status tham khảo

```text
CREATED -> PENDING -> SUCCEEDED
                   -> FAILED
                   -> EXPIRED
                   -> UNKNOWN

SUCCEEDED -> REFUND_PENDING -> REFUNDED
```

Payment status không nhất thiết giống order status. Một order có thể có nhiều payment attempt; cần xác định chính sách cho phép retry thanh toán và ngăn nhiều attempt cùng được capture ngoài ý muốn.

### 8.4. Inventory reservation status

```text
ACTIVE -> CONSUMED
       -> RELEASED
       -> EXPIRED
```

Reservation hết hạn phải được phối hợp với payment/order state. Không được giải phóng hàng một cách mù quáng nếu payment đã thành công nhưng webhook đang đến chậm.

## 9. Idempotency và xử lý request lặp

Với thao tác tạo order hoặc khởi tạo payment, client nên gửi `Idempotency-Key`.

Yêu cầu thiết kế:

1. Lưu key trong phạm vi user/customer và operation.
2. Có unique constraint ở database, không chỉ kiểm tra bằng Java.
3. Hai request đồng thời cùng key chỉ tạo một kết quả nghiệp vụ.
4. Cùng key nhưng payload khác phải bị từ chối hoặc xử lý theo policy đã công bố.
5. Lưu kết quả đủ để trả lại response phù hợp khi client retry.
6. Có thời hạn lưu và cơ chế dọn dữ liệu.
7. Không dùng key của client thay cho transaction ID của payment provider.

Webhook cũng phải idempotent. Nên lưu provider event ID hoặc transaction reference với unique constraint để tránh xử lý cùng một event nhiều lần.

## 10. Concurrency và tranh chấp tồn kho

Không dùng quy trình thiếu an toàn:

1. SELECT số lượng tồn kho.
2. Kiểm tra số lượng trong Java.
3. UPDATE giảm stock không có điều kiện.

Hai transaction có thể cùng đọc một đơn vị hàng cuối cùng và đều bán thành công.

Các chiến lược có thể chọn:

### 10.1. Atomic conditional update

```sql
UPDATE inventory
SET available_quantity = available_quantity - :quantity
WHERE sku_id = :skuId
  AND available_quantity >= :quantity;
```

Kiểm tra số dòng được cập nhật. Nếu bằng 0 thì không đủ hàng hoặc SKU không tồn tại.

### 10.2. Optimistic locking

Dùng version column (`@Version`) khi xung đột không quá thường xuyên. Khi phát sinh optimistic lock exception, xử lý retry có giới hạn và bảo đảm không lặp side effect không an toàn.

### 10.3. Pessimistic locking

Dùng `SELECT ... FOR UPDATE`/JPA pessimistic lock khi cần tuần tự hóa thao tác trên bản ghi stock. Cần chú ý lock duration, throughput và deadlock.

### 10.4. Reservation

Giữ hàng trong khoảng thời gian thanh toán. Reservation cần expiry, consume và release rõ ràng.

Nếu order có nhiều SKU, phải có chiến lược xử lý khi một SKU không đủ hàng: rollback toàn bộ transaction/reservation hoặc giải phóng các reservation đã tạo. Khi khóa nhiều SKU, nên truy cập theo thứ tự ổn định để giảm deadlock.

## 11. Payment timeout, webhook và reconciliation

Các nguyên tắc bắt buộc cần cân nhắc:

- Không giữ transaction database mở trong lúc gọi HTTP tới payment provider.
- Dùng timeout hữu hạn.
- Chỉ retry request thanh toán khi thao tác an toàn hoặc provider hỗ trợ idempotency.
- Timeout không đồng nghĩa payment thất bại; trạng thái có thể là `PENDING` hoặc `UNKNOWN`.
- Xác minh chữ ký webhook và cơ chế chống replay theo tài liệu provider.
- Không tin trạng thái thanh toán từ browser redirect nếu chưa xác minh server-side.
- Webhook có thể đến lặp hoặc sai thứ tự; xử lý theo event ID và state transition hợp lệ.
- Có reconciliation job để truy vấn provider và đối chiếu các giao dịch pending/unknown.
- Refund là workflow riêng, không phải rollback database.

Nếu payment thành công nhưng cập nhật order thất bại tạm thời, cần retry/reconciliation để đưa các trạng thái về nhất quán. Không được giả định một transaction SQL có thể rollback giao dịch đã hoàn tất ở provider.

## 12. Transaction và Outbox Pattern

Khi một thay đổi database cần phát event, có thể ghi outbox record trong cùng transaction:

```text
BEGIN TRANSACTION
  UPDATE order/payment state
  INSERT INTO outbox_event (...)
COMMIT
```

Worker đọc outbox và publish event. Sau khi publish, worker đánh dấu event đã xử lý.

Outbox giảm rủi ro database commit thành công nhưng event không được gửi. Tuy nhiên, outbox không tự tạo exactly-once end-to-end. Consumer vẫn phải idempotent vì message có thể được giao lại.

Không bắt buộc triển khai Kafka ngay. Có thể bắt đầu bằng bảng outbox và worker phù hợp với quy mô hiện tại.

## 13. Persistence và JPA

### 13.1. Migration

Có thể sử dụng Flyway hoặc Liquibase. Không nên dựa vào Hibernate `ddl-auto=update` làm cơ chế migration production.

Quy ước tham khảo:

- Migration được version control.
- Schema change được review.
- Production dùng migration có kiểm soát.
- Index và unique constraint phản ánh invariant nghiệp vụ.
- Cân nhắc backward-compatible migration nếu cần rolling deployment.

### 13.2. Entity và quan hệ

- Không serialize JPA entity trực tiếp thành API response.
- Tránh `FetchType.EAGER` nếu không có lý do rõ ràng.
- Theo dõi N+1 query.
- Không dùng `CascadeType.ALL`/`orphanRemoval` theo thói quen; phải hiểu lifecycle.
- Tránh `equals/hashCode` dựa trên association hoặc field mutable.
- Dùng pagination cho danh sách lớn.
- Dùng `BigDecimal` cho tiền, không dùng `double`.
- Lưu currency rõ ràng.
- Quy định timezone nhất quán; thường lưu timestamp hệ thống theo UTC/Instant.

### 13.3. BaseEntity

Không bắt buộc mọi domain phải có `BaseEntity`.

Nếu các persistence entity thực sự chia sẻ metadata như `id`, `createdAt`, `updatedAt`, có thể dùng `@MappedSuperclass`. Tuy nhiên:

- Đây là lựa chọn kỹ thuật JPA, không phải yêu cầu DDD.
- Không ép mọi aggregate có cùng lifecycle.
- Không đưa business behavior chung vào base class chỉ để tái sử dụng.
- Không dùng kế thừa nếu composition đơn giản và rõ ràng hơn.

## 14. REST API conventions

### 14.1. Versioning

Prefix tham khảo:

```text
/api/v1
```

Ví dụ endpoint:

```text
POST   /api/v1/orders
GET    /api/v1/orders/{orderId}
POST   /api/v1/orders/{orderId}/cancel

GET    /api/v1/products
GET    /api/v1/products/{productId}

POST   /api/v1/cart/items
PATCH  /api/v1/cart/items/{itemId}
DELETE /api/v1/cart/items/{itemId}
```

### 14.2. Request DTO

Ví dụ minh họa:

```java
public record PlaceOrderRequest(
    @NotNull UUID cartId,
    @NotBlank String shippingAddressId,
    @NotBlank String paymentMethod
) {}
```

Không tin `userId`, giá, discount hoặc tổng tiền do client gửi để quyết định quyền hoặc số tiền thu. Backend phải xác định actor từ security context và tính lại giá theo dữ liệu/policy có thẩm quyền.

### 14.3. Error response

Ví dụ:

```json
{
  "code": "INSUFFICIENT_STOCK",
  "message": "Không đủ số lượng tồn kho.",
  "requestId": "req-example",
  "details": []
}
```

Quy tắc:

- `code` ổn định để frontend xử lý.
- `message` thân thiện và có thể bản địa hóa.
- Không trả stack trace, SQL hoặc secret.
- Validation error nên chỉ rõ field.
- Dùng HTTP status phù hợp như `400`, `401`, `403`, `404`, `409`, `422` (nếu quy ước API sử dụng), `429`, `500`, `503`.

### 14.4. Security

- Xác thực và phân quyền ở backend.
- Kiểm tra ownership khi truy cập order/cart/address.
- CORS không thay thế authentication/authorization.
- Webhook có cơ chế xác thực riêng.
- Cân nhắc rate limit cho login, OTP và checkout.
- Không log access token, password hoặc dữ liệu thanh toán nhạy cảm.

## 15. Testing strategy

### 15.1. Domain unit tests

Kiểm tra business rules không cần Spring context:

- Order không chuyển trạng thái tùy ý.
- Không tạo order line với quantity không hợp lệ.
- Hủy order chỉ được phép ở trạng thái hợp lệ.
- Tính toán tiền và currency chính xác.

### 15.2. Application tests

Mock/fake các port để kiểm tra orchestration:

- Place order gọi inventory đúng.
- Thiếu hàng thì không tạo order thành công.
- Payment provider lỗi không làm payment bị đánh dấu thành công.
- User không có quyền không thể thao tác order của người khác.

### 15.3. Persistence integration tests

Ưu tiên Testcontainers với PostgreSQL khi cần xác minh:

- Transaction/rollback.
- Unique constraint.
- Locking/concurrency.
- Migration.
- JPA mapping và query.

H2 không phải lúc nào cũng thay thế được PostgreSQL, nhất là với SQL và locking behavior.

### 15.4. API tests

Kiểm tra:

- Validation.
- HTTP status và response schema.
- Authentication/authorization.
- Mapping lỗi nghiệp vụ.
- Không lộ field nhạy cảm.

### 15.5. Các bài test concurrency quan trọng

1. Hai request mua SKU chỉ còn một đơn vị.
2. Hai request cùng idempotency key.
3. Webhook cùng event ID gửi đồng thời.
4. Cancel order và payment success xảy ra đồng thời.
5. Outbox worker chạy nhiều instance.
6. Một SKU trong giỏ nhiều sản phẩm không đủ hàng.

## 16. Công nghệ tham khảo

| Thành phần | Lựa chọn tham khảo | Ghi chú |
|---|---|---|
| Language | Java 21+ | Chốt theo runtime mục tiêu |
| Framework | Spring Boot 3.x | Web, validation, security |
| Persistence | Spring Data JPA/Hibernate | Chọn query phù hợp workload |
| Database | PostgreSQL | Cần migration và backup |
| Migration | Flyway/Liquibase | Chọn một công cụ |
| Cache | Redis | Chỉ thêm khi có use case |
| Messaging | Kafka/RabbitMQ | Chỉ thêm khi cần async/event delivery |
| Testing | JUnit 5, Mockito, Testcontainers | Test DB thật cho behavior quan trọng |
| API docs | OpenAPI | Contract cho frontend/integration |
| Observability | Structured logs, metrics, tracing | Theo dõi request và workflow |

Danh sách trên là đề xuất, không phải yêu cầu bắt buộc phải triển khai toàn bộ ngay từ phiên bản đầu tiên.

## 17. Lộ trình triển khai

### Giai đoạn 1 — Foundation

- Khởi tạo Spring Boot project.
- Cấu hình Java/toolchain và profiles.
- Thiết lập migration.
- Thiết lập validation, exception handling, logging.
- Thiết lập CI chạy build và test.
- Tạo package/module boundaries và ADR.

### Giai đoạn 2 — Identity và Catalog

- Account/authentication/authorization.
- Product/category/variant.
- API pagination và validation.
- Integration test database.

### Giai đoạn 3 — Cart và Inventory

- Cart và CartItem.
- SKU/stock model.
- Chọn chiến lược concurrency.
- Test tranh chấp tồn kho.

### Giai đoạn 4 — Checkout và Order

- Place order use case.
- Tính lại giá ở backend.
- Snapshot order line và địa chỉ.
- Idempotency cho create order.
- Order state machine và inventory reservation.

### Giai đoạn 5 — Payment

- Payment gateway abstraction.
- Payment attempt.
- Webhook validation và idempotency.
- Timeout/unknown status.
- Reconciliation.
- Refund workflow nếu nằm trong scope.

### Giai đoạn 6 — Shipping và Notification

- Shipment lifecycle.
- Tracking integration.
- Notification delivery/retry.
- Outbox nếu cần phát event bền vững.

### Giai đoạn 7 — Production hardening

- Load test theo workload mục tiêu.
- Review query/index/deadlock.
- Security review và secret management.
- Metrics/alerts cho payment pending, reservation hết hạn và outbox backlog.
- Backup/restore drill và runbook.

## 18. Definition of Done

Một use case được coi là hoàn tất khi:

- Business rules và state transition được xác định.
- Có validation và authorization phù hợp.
- Có xử lý lỗi, timeout, retry và idempotency theo semantics của use case.
- Có unit/integration tests tương ứng.
- Có log/metrics đủ chẩn đoán mà không lộ dữ liệu nhạy cảm.
- API contract và database migration được review.
- Ranh giới module không bị phá vỡ.
- Tài liệu được cập nhật khi thay đổi domain, trạng thái hoặc kiến trúc.

## 19. Các quyết định cần xác nhận trước khi triển khai production

Các mục sau chưa thể chốt chỉ từ kiến trúc tổng quát:

- Một kho hay nhiều kho; cách phân bổ stock.
- Có giữ hàng trong lúc thanh toán không và reservation timeout bao lâu.
- Một order có cho phép nhiều payment attempt không.
- Payment provider nào được dùng và semantics webhook/retry của provider.
- Quy tắc hủy đơn, hoàn tiền và hoàn hàng.
- Cách tính promotion, shipping fee, tax và rounding.
- Có cần multi-currency không.
- Quy mô tải, SLA và yêu cầu availability.
- Chính sách lưu trữ/audit dữ liệu và yêu cầu pháp lý.
- Có cần Redis/message broker ngay ở phiên bản đầu không.

Các quyết định này nên được ghi lại thành Architecture Decision Records (ADR) riêng khi được thống nhất.
