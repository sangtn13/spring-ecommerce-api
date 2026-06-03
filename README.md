# 🛍️ SShop - Simple eCommerce API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

SShop is a Spring Boot REST API for an eCommerce system. It provides authentication, user management, product catalog management, categories, cart operations, order processing, image handling, and PayOS payment integration.

## 📋 Table of Contents

- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [System Requirements](#-system-requirements)
- [Installation & Setup](#-installation--setup)
- [Security Configuration](#-security-configuration)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Database](#-database)
- [Testing](#-testing)

## 🚀 Key Features

### 🔐 Authentication & Authorization
- JWT authentication with access and refresh tokens
- Redis-backed access token blacklist for logout and password changes
- Role-based access control with `User`, `Admin`, and `Manager`
- Protected endpoints with Spring Security
- Account lock support

### 👤 User Management
- User registration and login
- Logout with access-token invalidation
- Change password for the authenticated user
- Forgot password and reset password flows backed by Redis reset tokens and SMTP email delivery
- Current profile retrieval
- Admin user creation and update
- Role update, lock, and unlock actions

### 📦 Product Management
- Product CRUD operations
- Product filtering by brand ID, category ID, product name full-text search, and price range
- Product writes reference existing brand and category records by ID
- Brand separation from product rows for safer catalog growth
- Product inventory tracking

### 🏷️ Category & Brand Management
- Category CRUD operations
- Category lookup by ID or name
- Pagination support for category listing
- Brand CRUD operations
- Brand lookup by ID or name
- Pagination support for brand listing

### 🛒 Cart & Orders
- Add, update, and remove cart items
- Cart item update/remove uses `cartItemId` semantics on `/api/v1/cart/items/{itemId}`
- Retrieve current cart and total amount
- Place orders from the current cart
- Track current user's order history
- Cancel the current user's own order when the current status allows transition to `CANCELED`
- Update order status for admin and manager roles

### 🖼️ Image Management
- Upload multiple product images
- Download stored images
- Update or delete existing images

### 💳 Payments
- Create PayOS checkout links
- Receive PayOS webhook callbacks
- Mark a payment as canceled from the frontend when the user exits the PayOS flow
- Recreate a fresh PayOS checkout link after a canceled payment instead of reusing an old processed link

## 💻 Tech Stack

### Backend Framework
- **Spring Boot 3.5.6**
- **Spring Security**
- **Spring Data JPA**
- **Spring Validation**

### Database & Persistence
- **MySQL 8.0+**
- **Hibernate**
- **Flyway**

### API & Documentation
- **springdoc OpenAPI**
- **Swagger UI**

### Utilities
- **Lombok**
- **MapStruct**
- **Maven Wrapper**

## 🧰 System Requirements

- **Java 17** or higher
- **MySQL 8.0+**
- **Maven 3.6+** or the included Maven Wrapper

## 🔧 Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/sangtn13/spring-ecommerce-api.git
cd spring-ecommerce-api
```

### 2. Create the application properties file

Copy `src/main/resources/application.properties.template` to `src/main/resources/application.properties`.

### 3. Configure the application

Update the values in `src/main/resources/application.properties`:

```properties
server.port=5050

spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name?connectionTimeZone=Asia/Ho_Chi_Minh
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.data.redis.host=localhost
spring.data.redis.port=6379

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_SMTP_USERNAME
spring.mail.password=YOUR_SMTP_PASSWORD_OR_APP_PASSWORD

api.prefix=/api/v1

sshop.app.jwtSecret=PLEASE_GENERATE_YOUR_OWN_JWT_SECRET_KEY_HERE
sshop.app.jwtExpirationMs=3600000
sshop.app.refreshTokenExpirationMs=604800000
sshop.app.passwordResetTokenExpirationMs=900000
sshop.app.resetPasswordBaseUrl=http://localhost:3000/reset-password
sshop.mail.fromName=SShop
sshop.mail.fromAddress=YOUR_SMTP_USERNAME
sshop.mail.supportEmail=support@example.com

sshop.seed.user.password=
sshop.seed.admin.password=

payos.client-id=
payos.api-key=
payos.checksum-key=
payos.return-url-base=
payos.cancel-url-base=
```

### 4. Create the database

Example:

```sql
CREATE DATABASE sshop_db;
```

Then point `spring.datasource.url` to that database.

### 5. Optional seed accounts

Seed accounts are created **only if** you provide values for:

- `sshop.seed.user.password`
- `sshop.seed.admin.password`

If configured, the application can create:

- `admin@gmail.com` with role `Admin`
- `user1@gmail.com` to `user5@gmail.com` with role `User`

### 6. Run the application

#### Using Maven Wrapper

```bash
.\mvnw.cmd spring-boot:run
```

#### Build and run the JAR

```bash
.\mvnw.cmd clean install
java -jar target/sshop-0.0.1-SNAPSHOT.jar
```

The application runs by default at:

```text
http://localhost:5050
```

### 7. Optional Docker services

The repository includes `docker-compose.yaml` for:

- MySQL
- Redis
- SonarQube
- PostgreSQL for SonarQube

Before running Docker Compose, copy `.env.example` to `.env` and update the values used by Compose.

If the Spring Boot app runs on your machine while Redis runs in Docker, keep:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

If the app later runs in Docker on the same Compose network, set:

```text
REDIS_HOST=redis
REDIS_PORT=6379
```

For password reset email, fill these placeholders in `src/main/resources/application.properties`:

```text
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-smtp-username
MAIL_PASSWORD=your-smtp-password-or-app-password
MAIL_FROM_NAME=SShop
MAIL_FROM_ADDRESS=your-sender-address
MAIL_SUPPORT_EMAIL=support@example.com
RESET_PASSWORD_BASE_URL=http://localhost:3000/reset-password
```

`RESET_PASSWORD_BASE_URL` should point to your frontend page that reads the `token` query parameter and lets the user submit a new password.

## 🔒 Security Configuration

### Public endpoints

- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/api/v1/auth/refresh`
- `/api/v1/auth/forgot-password`
- `/api/v1/auth/reset-password`
- `/api/v1/products/**`
- `/api/v1/categories/**`
- `/api/v1/brands/**`
- `/api/v1/payments/payos-webhook`

### Authenticated endpoints

- `/api/v1/auth/logout`
- `/api/v1/auth/change-password`
- `/api/v1/users/**`
- `/api/v1/orders/**`
- `/api/v1/cart`
- `/api/v1/cart/**`
- `/api/v1/images/**`
- `/api/v1/payments/orders/**`

### Role-restricted endpoints

- `Admin` manages users
- `Admin` and `Manager` manage products, categories, images, and order status updates

## 📚 API Documentation

- API reference: `FRONTEND_API.md`
- Swagger UI: `http://localhost:5050/swagger-ui.html`
- OpenAPI docs: `http://localhost:5050/api-docs`

## 🏗️ Project Structure

```text
src/
├── main/
│   ├── java/com/ecommerce/sshop/
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access layer
│   │   ├── model/               # Entities
│   │   ├── dto/                 # Response/data transfer models
│   │   ├── request/             # Request payload models
│   │   ├── response/            # Common response wrappers
│   │   ├── exception/           # Exception handling
│   │   ├── security/            # Security configuration
│   │   ├── enums/               # Enums
│   │   ├── data/                # Data initialization
│   │   └── SshopApplication.java
│   └── resources/
│       ├── application.properties.template
│       └── db/migration/
└── test/
```

## 🗄️ Database

### Main areas

- **users / roles**: authentication and authorization
- **brands / products / categories / images**: product catalog
- **carts / cart_items**: shopping cart state
- **orders / order_items / payments**: checkout and payment lifecycle

### Notes

- Flyway migrations are stored in `src/main/resources/db/migration`
- JPA timezone is configured for `Asia/Ho_Chi_Minh`

## 🧪 Testing

Run tests with:

```bash
.\mvnw.cmd test
```
