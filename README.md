# E-Commerce Decomposition – 3 Spring Boot Services

Project này triển khai 4 bài tập dưới dạng **3 Spring Boot project độc lập**:

- `customer-service` — port `8081` — database `customer_db`
- `product-service` — port `8082` — database `product_db`
- `order-service` — port `8083` — database `order_db`

Mỗi service có `build.gradle` riêng, source code riêng và database riêng. Các service **không import code của nhau** và **không dùng chung bảng/database**.

---

## 1. Bài tập 1 – Thiết kế kiến trúc phân rã

### 1.1. Ranh giới service

```text
┌──────────────────────────┐
│     Customer Service     │
│        :8081             │
│  DB: customer_db         │
│                          │
│ Customer                 │
│ - id                     │
│ - fullName               │
│ - email                  │
│ - password               │
│ - address                │
└────────────┬─────────────┘
             │ HTTP/JSON
             │ chỉ trao đổi qua API
             ▼
┌──────────────────────────┐
│       Order Service      │
│        :8083             │
│  DB: order_db            │
│                          │
│ Order                    │
│ - id                     │
│ - customerId             │
│ - productId              │
│ - orderDate              │
│ - totalAmount            │
│ - status                 │
└────────────┬─────────────┘
             │ HTTP/JSON
             │ GET product
             ▼
┌──────────────────────────┐
│      Product Service     │
│        :8082             │
│  DB: product_db          │
│                          │
│ Product                  │
│ - id                     │
│ - name                   │
│ - price                  │
│ - stockQuantity          │
│ - description            │
└──────────────────────────┘
```

### 1.2. Vì sao Order chỉ lưu `customerId` mà không lưu `Customer`?

Trong kiến trúc Microservices, `Customer` thuộc quyền quản lý của Customer Service và nằm trong `customer_db`. Order Service không sở hữu bảng `customers`, vì vậy Order Service **không nên tạo quan hệ JPA `@ManyToOne` hoặc `@JoinColumn`** sang Customer.

Order chỉ lưu:

```java
private Long customerId;
```

Lý do:

1. **Database độc lập:** `customer_db` và `order_db` là hai database khác nhau.
2. **Không phụ thuộc code:** Order Service không cần import class `Customer` từ Customer Service.
3. **Giảm coupling:** Customer Service có thể thay đổi entity nội bộ mà không buộc Order Service build lại.
4. **Triển khai độc lập:** một service có thể deploy/bảo trì riêng.
5. **Đúng ownership của dữ liệu:** Customer Service là nơi duy nhất quản lý Customer.
6. Khi cần thông tin khách hàng, Order Service có thể gọi API của Customer Service thay vì JOIN trực tiếp database.

Tương tự, Order chỉ lưu `productId`. Khi tạo đơn, project này minh họa việc Order Service gọi Product Service qua HTTP để lấy giá sản phẩm.

---

## 2. Yêu cầu môi trường

- JDK 17
- PostgreSQL
- Gradle (hoặc import từng project vào IntelliJ để IDE tự tải Gradle)
- Postman hoặc IntelliJ HTTP Client

---

## 3. Tạo database PostgreSQL

Có 2 cách.

### Cách 1 – Docker Compose

Tại thư mục gốc:

```bash
docker compose up -d
```

Container PostgreSQL chạy ở:

```text
host: localhost
port: 5432
username: postgres
password: 123456
```

File `init-databases.sql` tạo:

```text
customer_db
product_db
order_db
```

### Cách 2 – Tạo thủ công bằng pgAdmin / psql

```sql
CREATE DATABASE customer_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
```

Nếu PostgreSQL của bạn dùng username/password khác, sửa `application.properties` trong từng service.

---

## 4. Chạy các service

Mở **3 terminal**.

### Customer Service

```bash
cd customer-service
gradle bootRun
```

Chạy tại:

```text
http://localhost:8081
```

### Product Service

```bash
cd product-service
gradle bootRun
```

Chạy tại:

```text
http://localhost:8082
```

### Order Service

```bash
cd order-service
gradle bootRun
```

Chạy tại:

```text
http://localhost:8083
```

> Nên chạy Product Service trước Order Service vì khi tạo order, Order Service sẽ gọi Product Service để lấy giá.

---

# Bài tập 2 – Customer Service

## 5. Register Customer

```http
POST http://localhost:8081/api/v1/customers/register
Content-Type: application/json

{
  "fullName": "Nguyen Van A",
  "email": "a@gmail.com",
  "password": "123456",
  "address": "Ha Noi"
}
```

Mật khẩu được hash bằng BCrypt trước khi lưu database.

Response không có password:

```json
{
  "id": 1,
  "fullName": "Nguyen Van A",
  "email": "a@gmail.com",
  "address": "Ha Noi"
}
```

## 6. GET Customer

```http
GET http://localhost:8081/api/v1/customers/1
```

Nếu gọi:

```http
GET http://localhost:8081/api/v1/customers/99
```

thì nhận:

```json
{
  "timestamp": "2026-10-12T09:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id = 99"
}
```

## 7. Login

```http
PUT http://localhost:8081/api/v1/customers/login
Content-Type: application/json

{
  "email": "a@gmail.com",
  "password": "123456"
}
```

Nếu sai email hoặc password:

```json
{
  "timestamp": "2026-10-12T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "email or password incorrect"
}
```

---

# Bài tập 3 – Product Service & Validation

## 8. Tạo product hợp lệ

```http
POST http://localhost:8082/api/v1/products
Content-Type: application/json

{
  "name": "Laptop Asus",
  "price": 20000000,
  "stockQuantity": 10,
  "description": "Laptop van phong"
}
```

## 9. Test Bean Validation

```http
POST http://localhost:8082/api/v1/products
Content-Type: application/json

{
  "name": "",
  "price": -500,
  "stockQuantity": -1,
  "description": "Invalid product"
}
```

Response:

```json
{
  "timestamp": "2026-10-12T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "name: Name must not be blank; price: Price must be greater than 0; stockQuantity: Stock quantity must not be negative"
}
```

Dữ liệu validation thất bại sẽ không được lưu DB.

---

# Bài tập 4 – Order Service

## 10. Tạo Order

Trước tiên phải có Product với `id = 1`.

```http
POST http://localhost:8083/api/v1/orders
Content-Type: application/json

{
  "customerId": 1,
  "productId": 1,
  "quantity": 2
}
```

Luồng xử lý:

```text
Client
  |
  | POST /api/v1/orders
  v
OrderController
  |
  v
OrderService
  |
  | HTTP GET http://localhost:8082/api/v1/products/{productId}
  v
Product Service
  |
  | trả price
  v
OrderService
  |
  | totalAmount = price × quantity
  v
OrderRepository
  |
  v
order_db
```

Ví dụ Product có giá `20,000,000` và quantity = `2`:

```json
{
  "id": 1,
  "customerId": 1,
  "productId": 1,
  "orderDate": "2026-10-12T09:30:00",
  "totalAmount": 40000000,
  "status": "CREATED"
}
```

## 11. Quantity <= 0

```http
POST http://localhost:8083/api/v1/orders
Content-Type: application/json

{
  "customerId": 1,
  "productId": 1,
  "quantity": 0
}
```

Response:

```json
{
  "timestamp": "2026-10-12T09:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Quantity must be greater than 0"
}
```

## 12. GET Order

```http
GET http://localhost:8083/api/v1/orders/1
```

---

## 13. Tại sao không dùng `@JoinColumn`?

Sai trong bài này:

```java
@ManyToOne
@JoinColumn(name = "customer_id")
private Customer customer;
```

vì `Customer` không thuộc Order Service và bảng `customers` không nằm trong `order_db`.

Thiết kế đúng:

```java
@Column(nullable = false)
private Long customerId;

@Column(nullable = false)
private Long productId;
```

Quan hệ giữa các service là **logical reference bằng ID**, không phải foreign key vật lý giữa các database.

---

## 14. Cấu trúc project

```text
ecommerce-decomposition-3-services/
│
├── README.md
├── docker-compose.yml
├── init-databases.sql
├── api-tests.http
├── Ecommerce-3-Services.postman_collection.json
│
├── customer-service/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/main/
│       ├── java/com/example/customerservice/
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── entity/
│       │   ├── exception/
│       │   ├── repository/
│       │   └── service/
│       └── resources/application.properties
│
├── product-service/
│   ├── build.gradle
│   ├── settings.gradle
│   └── src/main/...
│
└── order-service/
    ├── build.gradle
    ├── settings.gradle
    └── src/main/...
```

Ba thư mục service là **ba project Gradle độc lập**, không khai báo chung trong một `settings.gradle`.
