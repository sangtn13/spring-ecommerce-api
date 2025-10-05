# 🛍️ SShop - Simple eCommerce API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A simple eCommerce REST API built with Spring Boot, providing essential features for an online shopping system including product management, user management, shopping cart, and order processing.

## 📋 Table of Contents

- [Key Features](#-key-features)
- [Tech Stack](#-tech-stack)
- [Installation & Setup](#-installation--setup)
- [Security Configuration](#-security-configuration)
- [API Endpoints](#-api-endpoints)
- [Project Structure](#-project-structure)
- [Database](#-database)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

## 🚀 Key Features

### 🔐 Authentication & Authorization
- **JWT Authentication**: User authentication using JSON Web Token
- **Role-based Access Control**: Permission management based on roles (User, Admin)
- **Password Encryption**: Password hashing using BCrypt
- **Secure API Endpoints**: Protection of sensitive endpoints

### 👤 User Management
- User registration and login
- Profile information updates
- Role and permission management
- Automatic sample data initialization (Admin, Users)

### 📦 Product Management
- CRUD operations for products
- Product categorization
- Product search by name, brand, category
- Product image management
- Inventory tracking

### 🛒 Shopping Cart & Orders
- Add/remove/update products in cart
- Automatic price calculation
- Order placement and status tracking
- User purchase history

### 🏷️ Category Management
- Add/edit/delete product categories
- Link products with categories

### 🖼️ Image Management
- Upload and store product images
- Image download functionality
- Update and delete images

## 💻 Tech Stack

### Backend Framework
- **Spring Boot 3.5.6** - Core framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Object-Relational Mapping
- **Spring Validation** - Input data validation

### Database
- **MySQL 8.0+** - Primary database
- **Hibernate** - ORM framework

### Security & Authentication
- **JWT (JSON Web Tokens)** - Token-based authentication
- **BCrypt** - Password hashing
- **JJWT** - JWT implementation for Java

### Utilities
- **Lombok** - Reduce boilerplate code
- **ModelMapper** - Object mapping
- **Maven** - Dependency management

### Development Tools
- **Java 17** - Programming language
- **Maven Wrapper** - Build tool
- **Spring Boot DevTools** - Development utilities

## 🔧 Installation & Setup

### System Requirements
- **Java 17** or higher
- **MySQL 8.0+**
- **Maven 3.6+** (or use the included Maven Wrapper)

### 1. Clone repository
```bash
git clone https://github.com/SangTran13/spring-ecommerce-api.git
cd sshop
```

### 2. Database Setup
Create MySQL database:
```sql
CREATE DATABASE sshop_db;
```

### 3. Application Configuration
Edit `src/main/resources/application.properties` with your database information if needed:
```properties
# Database configuration (default setup)
spring.datasource.url=jdbc:mysql://localhost:3306/sshop_db?allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=admin

# JWT Configuration (already configured with a default secret)
sshop.app.jwtSecret=g0qlJwfjNUHoDn4YOos9jItP5/srQ3QXbPwJjzQFfyTTKpVH+NRLFSGgErlYp3KnThZ+tXBmHms5ysdmk8WL6g==
sshop.app.jwtExpirationMs=3600000
```

**Note:** The application is pre-configured for local development. Just make sure your MySQL credentials match (default: root/admin).

### 4. Build and Run Application

#### Using Maven Wrapper (Recommended):
```bash
# Build the application
./mvnw clean install    # Linux/Mac
.\mvnw.cmd clean install    # Windows

# Run the application
java -jar target/sshop-0.0.1-SNAPSHOT.jar
```

#### Or run directly with Maven:
```bash
./mvnw spring-boot:run    # Linux/Mac
.\mvnw.cmd spring-boot:run    # Windows
```

The application will run at: `http://localhost:5050`

## 🔒 Security Configuration

### Default Accounts
The application automatically creates sample accounts on startup:

**Admin Account:**
- Email: `admin@gmail.com`
- Password: `123456`
- Role: Admin

**User Accounts:**
- Email: `user1@gmail.com` to `user5@gmail.com`
- Password: `1234561` to `1234565` (respectively)
- Role: User

### Production Security
For production environments, please:
1. Change all default passwords
2. Use strong JWT secret key
3. Configure HTTPS
4. Set `spring.jpa.hibernate.ddl-auto=validate`

See [SECURITY.md](SECURITY.md) for detailed information

## 📚 API Endpoints

### Authentication
```http
POST /api/v1/auth/login
```

### Users
```http
GET    /api/v1/users/{userId}        # Get user information
POST   /api/v1/users                # Create new user
PUT    /api/v1/users/{userId}       # Update user
DELETE /api/v1/users/{userId}       # Delete user
```

### Products
```http
GET    /api/v1/products                           # Get all products
GET    /api/v1/products/{id}                      # Get product by ID
POST   /api/v1/products                          # Add new product (Admin)
PUT    /api/v1/products/{id}                     # Update product (Admin)
DELETE /api/v1/products/{id}                     # Delete product (Admin)
GET    /api/v1/products/by-category/{category}   # Get products by category
GET    /api/v1/products/by-brand                 # Get products by brand
GET    /api/v1/products/name/{name}              # Search products by name
```

### Categories
```http
GET    /api/v1/categories           # Get all categories
POST   /api/v1/categories          # Create new category
GET    /api/v1/categories/{id}     # Get category by ID
PUT    /api/v1/categories/{id}     # Update category
DELETE /api/v1/categories/{id}     # Delete category
```

### Cart & Cart Items
```http
GET    /api/v1/carts/{cartId}                    # Get cart
DELETE /api/v1/carts/{cartId}/clear             # Clear cart
POST   /api/v1/cart-items/add                   # Add item to cart
PUT    /api/v1/cart-items/{cartId}/update/{itemId}  # Update item quantity
DELETE /api/v1/cart-items/{cartId}/remove/{itemId}  # Remove item from cart
```

### Orders
```http
POST /api/v1/orders?userId={userId}      # Create order
GET  /api/v1/orders/{orderId}           # Get order details
GET  /api/v1/orders/user/{userId}       # Get user orders
```

### Images
```http
POST /api/v1/images/upload                    # Upload images
GET  /api/v1/images/download/{imageId}       # Download image
PUT  /api/v1/images/image/{imageId}/update   # Update image
DELETE /api/v1/images/image/{imageId}/delete # Delete image
```

### Swagger API Documentation
After running the application, you can access Swagger UI at:
- **Swagger UI**: `http://localhost:5050/swagger-ui.html`
- **API Docs**: `http://localhost:5050/api-docs`

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/ecommerce/sshop/
│   │   ├── controller/          # REST Controllers
│   │   ├── service/             # Business Logic Layer
│   │   ├── repository/          # Data Access Layer
│   │   ├── model/              # Entity Classes
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── request/            # Request DTOs
│   │   ├── response/           # Response DTOs
│   │   ├── exception/          # Custom Exceptions
│   │   ├── security/           # Security Configuration
│   │   ├── enums/             # Enum Classes
│   │   ├── data/              # Data Initialization
│   │   └── SshopApplication.java
│   └── resources/
│       ├── application.properties
│       ├── application-local.properties
│       └── application-production.properties.template
└── test/                       # Test Classes
```

## 🗄️ Database

### ERD (Entity Relationship Diagram)
```
Users ←→ Carts ←→ CartItems ←→ Products
  ↓                              ↑
Orders ←→ OrderItems              │
                                 │
                           Categories
                                 │
                              Images
```

### Main Tables:
- **users**: User information
- **roles**: User roles
- **user_roles**: Users-roles junction table
- **products**: Product information
- **categories**: Product categories
- **images**: Product images
- **carts**: Shopping carts
- **cart_items**: Items in shopping cart
- **orders**: Orders
- **order_items**: Items in orders

## 🧪 Testing

Run unit tests:
```bash
./mvnw test
```

Run integration tests:
```bash
./mvnw verify
```

## 🚀 Deployment

### Production Deployment
1. Create `application-production.properties` file
2. Configure production environment variables
3. Build production JAR:
```bash
./mvnw clean package -Pprod
```
4. Run with production profile:
```bash
java -jar target/sshop-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
```

### Docker Deployment (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/sshop-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 5050
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 📝 Important Notes

1. **Security**: Always change JWT secret key and database credentials in production
2. **CORS**: Configure CORS appropriately for frontend domain
3. **Rate Limiting**: Consider implementing rate limiting for production
4. **Monitoring**: Add monitoring and logging for production environment
5. **Backup**: Set up backup strategy for database

## 🤝 Contributing

1. Fork this repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is distributed under the MIT License. See `LICENSE` file for more information.

## 👨‍💻 Author

**Sang Tran** - [GitHub](https://github.com/SangTran13)

## 📞 Contact

If you have any questions, please create an issue or contact via email.

---

⭐ **If this project is helpful to you, don't forget to star the repository!** ⭐