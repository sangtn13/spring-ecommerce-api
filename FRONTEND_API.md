# SShop API Reference

## Base URL

```text
http://localhost:5050/api/v1
```

## Authentication

### Headers

| Header | Value |
| --- | --- |
| `Authorization` | `Bearer <accessToken>` |
| `X-Refresh-Token` | `<refreshToken>` |

### Current-user behavior

The following endpoints use the authenticated user from the access token and do not require a `userId` in the request:

- `GET /users/me`
- `GET /cart`
- `DELETE /cart/items`
- `GET /cart/total`
- `POST /orders`
- `GET /orders`
- all endpoints under `/cart/items`

## Response Format

### Standard response

```json
{
  "message": "Human readable message",
  "data": {}
}
```

### Paginated response

```json
{
  "message": "Success message",
  "data": {
    "content": [],
    "pageNumber": 1,
    "pageSize": 5,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

## Error Handling

### Common status codes

| Status | Meaning |
| --- | --- |
| `400` | Bad Request |
| `401` | Unauthorized |
| `403` | Forbidden |
| `404` | Not Found |
| `409` | Conflict |
| `423` | Locked |
| `500` | Internal Server Error |

### Error response

```json
{
  "message": "Error message",
  "data": null
}
```

### Unauthorized response

```json
{
  "error": "Unauthorized",
  "message": "You need to login to access this action"
}
```

## Pagination Rules

| Parameter | Value |
| --- | --- |
| `page` | 1-based |
| `size` | default `5`, max `20` |
| `sortBy` | default `id` |
| `sortDirection` | default `asc` |

## Access Summary

| Scope | Rules |
| --- | --- |
| Public | `/auth/**`, `/products/**`, `/categories/**`, `/brands/**`, `POST /payments/payos-webhook` |
| Authenticated | `/users/**`, `/orders/**`, `/cart`, `/cart/**`, `/images/**`, `/payments/orders/**` |
| Admin only | user management endpoints |
| Admin or Manager | product write endpoints, category write endpoints, all image endpoints, `GET /orders/{orderId}`, `PATCH /orders/{orderId}` |

## Data Shapes

### AuthResponse

```json
{
  "id": "user-id",
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token"
}
```

### UserDto

```json
{
  "id": "user-id",
  "firstName": "Sshop",
  "lastName": "User",
  "email": "user@gmail.com",
  "accountLocked": false,
  "lastLoginAt": "2026-06-02T10:30:00",
  "orders": [],
  "cart": {}
}
```

### ProductDto

```json
{
  "id": "product-id",
  "name": "iPhone 15",
  "brand": {
    "id": "brand-id",
    "name": "Apple"
  },
  "price": 999.99,
  "inventory": 10,
  "description": "Product description",
  "category": {
    "id": "category-id",
    "name": "Phones"
  },
  "images": [
    {
      "id": "image-id",
      "fileName": "iphone.jpg",
      "downloadUrl": "/api/v1/images/image-id"
    }
  ]
}
```

### CartDto

```json
{
  "cartId": "cart-id",
  "totalAmount": 1999.98,
  "items": [
    {
      "itemId": "item-id",
      "quantity": 2,
      "unitPrice": 999.99,
      "product": {}
    }
  ]
}
```

### OrderDto

```json
{
  "id": "order-id",
  "userId": "user-id",
  "orderDate": "2026-06-02",
  "totalAmount": 1999.98,
  "status": "PENDING",
  "orderItems": [
    {
      "productId": "product-id",
      "productName": "iPhone 15",
      "productBrand": "Apple",
      "quantity": 2,
      "price": 999.99
    }
  ]
}
```

## Endpoint Index

### Auth

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `POST` | `/auth/login` | Public | JSON body | `AuthResponse` |
| `POST` | `/auth/refresh` | Public | `X-Refresh-Token` header | `AuthResponse` |
| `POST` | `/auth/register` | Public | JSON body | `userId` |

### Users

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `GET` | `/users/me` | Authenticated | None | `UserDto` |
| `GET` | `/users` | `Admin` | Pagination query | Paginated `UserDto` list |
| `GET` | `/users/{userId}` | `Admin` | Path param | `UserDto` |
| `POST` | `/users` | `Admin` | JSON body | `UserDto` |
| `PUT` | `/users/{userId}` | `Admin` | JSON body | `UserDto` |
| `PATCH` | `/users/{userId}/roles` | `Admin` | JSON body | `UserDto` |
| `DELETE` | `/users/{userId}` | `Admin` | Path param | Empty data |
| `PATCH` | `/users/{userId}/account-lock` | `Admin` | JSON body | `UserDto` |

### Categories

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `GET` | `/categories` | Public | Pagination query | Paginated category list |
| `POST` | `/categories` | `Admin` or `Manager` | JSON body with `name` | Category |
| `GET` | `/categories/{id}` | Public | Path param | Category |
| `GET` | `/categories?name={name}` | Public | `name` query | Category |
| `PUT` | `/categories/{id}` | `Admin` or `Manager` | JSON body with `name` | Category |
| `DELETE` | `/categories/{id}` | `Admin` or `Manager` | Path param | Empty data |

### Brands

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `GET` | `/brands` | Public | Pagination query | Paginated brand list |
| `GET` | `/brands?name={name}` | Public | `name` query | Brand |
| `GET` | `/brands/{id}` | Public | Path param | Brand |
| `POST` | `/brands` | `Admin` or `Manager` | JSON body with `name` | Brand |
| `PUT` | `/brands/{id}` | `Admin` or `Manager` | JSON body with `name` | Brand |
| `DELETE` | `/brands/{id}` | `Admin` or `Manager` | Path param | Empty data |

### Products

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `GET` | `/products` | Public | Pagination query | Paginated `ProductDto` list |
| `GET` | `/products/{id}` | Public | Path param | `ProductDto` |
| `POST` | `/products` | `Admin` or `Manager` | JSON body | `ProductDto` |
| `PUT` | `/products/{productId}` | `Admin` or `Manager` | JSON body; omitted `brandId` / `categoryId` keep current relations | `ProductDto` |
| `DELETE` | `/products/{productId}` | `Admin` or `Manager` | Path param | Deleted product ID |
| `GET` | `/products?brandId={brandId}` | Public | brand ID + pagination query | Paginated `ProductDto` list |
| `GET` | `/products?categoryId={categoryId}` | Public | category ID + pagination query | Paginated `ProductDto` list |
| `GET` | `/products?name={name}` | Public | product name full-text search; short tokens also use phrase matching | Paginated `ProductDto` list |
| `GET` | `/products?minPrice={minPrice}&maxPrice={maxPrice}` | Public | price range + pagination query | Paginated `ProductDto` list |
| `GET` | `/products?brandId={brandId}&categoryId={categoryId}&name={name}` | Public | any combination of brand ID, category ID, name search, and price range | Paginated `ProductDto` list |

### Cart

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `GET` | `/cart` | Authenticated | None | `CartDto` |
| `DELETE` | `/cart/items` | Authenticated | None | Empty data |
| `GET` | `/cart/total` | Authenticated | None | `BigDecimal` |

### Cart Items

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `POST` | `/cart/items` | Authenticated | `productId`, `quantity` query | Empty data |
| `DELETE` | `/cart/items/{itemId}` | Authenticated | real `cartItemId` path param | Empty data |
| `PUT` | `/cart/items/{itemId}` | Authenticated | real `cartItemId` path param + `quantity` query | Empty data |

### Orders

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `POST` | `/orders` | Authenticated | None | `OrderDto` |
| `GET` | `/orders` | Authenticated | Pagination query | Paginated `OrderDto` list |
| `GET` | `/orders/{orderId}` | `Admin` or `Manager` | Path param | `OrderDto` |
| `PATCH` | `/orders/{orderId}` | `Admin` or `Manager` | JSON body | `OrderDto` |
| `PATCH` | `/orders/{orderId}/cancel` | Authenticated | Path param | `OrderDto` |

### Images

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `POST` | `/images` | `Admin` or `Manager` | Multipart `files` + `productId` | `ImageDto[]` |
| `GET` | `/images/{imageId}` | `Admin` or `Manager` | Path param | Binary file |
| `PUT` | `/images/{imageId}` | `Admin` or `Manager` | Multipart `file` | `ImageDto` |
| `DELETE` | `/images/{imageId}` | `Admin` or `Manager` | Path param | Empty data |

### Payments

| Method | Route | Access | Request | Success |
| --- | --- | --- | --- | --- |
| `POST` | `/payments/orders/{orderId}/checkout-link` | Authenticated | Path param | Checkout URL string |
| `POST` | `/payments/orders/{orderId}/cancel` | Authenticated | Path param | `"Canceled"` |
| `POST` | `/payments/payos-webhook` | Public | Request body from PayOS | Success message |

## Request Payload Examples

### `POST /auth/login`

```json
{
  "email": "user@gmail.com",
  "password": "123456"
}
```

### `POST /auth/register`

```json
{
  "firstName": "A",
  "lastName": "B",
  "email": "newuser@gmail.com",
  "password": "123456"
}
```

### `POST /users`

```json
{
  "firstName": "A",
  "lastName": "B",
  "email": "staff@gmail.com",
  "password": "123456",
  "roles": ["User"]
}
```

### `PUT /users/{userId}`

```json
{
  "firstName": "New",
  "lastName": "Name"
}
```

### `PATCH /users/{userId}/roles`

```json
{
  "roles": ["Manager", "User"]
}
```

### `POST /categories`

```json
{
  "name": "Phones"
}
```

### `PUT /categories/{id}`

```json
{
  "name": "Updated category name"
}
```

### `POST /products`

```json
{
  "name": "iPhone 15",
  "brandId": "brand-id",
  "price": 999.99,
  "inventory": 10,
  "description": "Latest model",
  "categoryId": "category-id"
}
```

Both `brandId` and `categoryId` are required. Product write APIs do not auto-create brands or categories.

### `PUT /products/{productId}`

Uses the same payload shape as `POST /products`.
If `brandId` or `categoryId` is omitted, the existing relation is kept unchanged.

### `PATCH /users/{userId}/account-lock`

```json
{
  "locked": true
}
```

### `PATCH /orders/{orderId}`

```json
{
  "status": "SHIPPED"
}
```

### `PATCH /orders/{orderId}/cancel`

No request body.

## Order Status Values

- `PENDING`
- `PROCESSING`
- `SHIPPED`
- `DELIVERED`
- `CANCELED`

## Notes

- `page` is 1-based, not 0-based
- product list filters use `brandId` / `categoryId`
- `name` uses full-text matching; when the input contains short tokens such as `14`, phrase matching is also applied to narrow the result set
- product create requires valid `brandId` and `categoryId`
- product update keeps the current brand/category when those IDs are not provided
- cart item operations use query parameters instead of JSON bodies
- `PUT /cart/items/{itemId}` and `DELETE /cart/items/{itemId}` require a real `cartItemId`, not `productId`
- image upload uses multipart field `files`
- image update uses multipart field `file`
- `POST /payments/orders/{orderId}/checkout-link` requires an access token
- `POST /payments/orders/{orderId}/checkout-link` reuses a previous checkout URL only while the payment is still `PENDING`
- if a payment is `CANCELED` or `FAILED`, creating a checkout link again returns a fresh PayOS link with a new `orderCode`
- `POST /payments/orders/{orderId}/cancel` lets frontend mark a payment as `CANCELED` after the user leaves the PayOS flow
- `PATCH /orders/{orderId}/cancel` only cancels the authenticated user's own order and only when status can transition to `CANCELED`
- missing cart item or payment records return `404`
