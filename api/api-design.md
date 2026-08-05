# REST API Design Document
## Book Store — E-Commerce Book Store Application

**Version:** 1.0  
**Date:** July 2025  
**Base URL:** `https://api.bookstore.com/api/v1`  
**Based on:** SRS v1.0 · Architecture v1.0 · Database Design v1.0

---

## Table of Contents

1. [API Design Principles](#1-api-design-principles)
2. [Authentication APIs](#2-authentication-apis)
3. [User APIs](#3-user-apis)
4. [Home APIs](#4-home-apis)
5. [Genre APIs](#5-genre-apis)
6. [Book APIs](#6-book-apis)
7. [Author APIs](#7-author-apis)
8. [Review APIs](#8-review-apis)
9. [Wishlist APIs](#9-wishlist-apis)
10. [Shopping Cart APIs](#10-shopping-cart-apis)
11. [Checkout APIs](#11-checkout-apis)
12. [Payment APIs](#12-payment-apis)
13. [Order APIs](#13-order-apis)

---

## 1. API Design Principles

### 1.1 REST Conventions

- All APIs follow REST architectural constraints: stateless, uniform interface, resource-based URLs.
- Resources are nouns, always plural: `/books`, `/users`, `/orders`.
- Sub-resources reflect ownership: `/users/me/addresses`, `/orders/{id}/items`.
- Actions that cannot be expressed as CRUD use verb-noun path segments under a resource: `/orders/{id}/buy-again`, `/cart/items/{itemId}`.
- All request and response bodies use `application/json`.

### 1.2 Resource Naming

| Pattern | Example | Use Case |
|---------|---------|----------|
| `/resources` | `/books` | Collection |
| `/resources/{id}` | `/books/42` | Single resource |
| `/resources/{id}/sub` | `/books/42/reviews` | Nested sub-resource |
| `/resources/{id}/actions` | `/orders/7/buy-again` | Non-CRUD action |
| `/resources/me` | `/users/me` | Authenticated user's own resource |

### 1.3 API Versioning Strategy

- All endpoints are prefixed with `/api/v1/`.
- Version is embedded in the URL path to allow parallel deployment of multiple API versions during migration periods.
- The version is incremented to `/api/v2/` only on breaking changes (field removals, semantic changes). Additive changes (new optional fields, new endpoints) do not require a version bump.

### 1.4 Authentication Strategy

- **Mechanism:** Stateless JWT (JSON Web Token) Bearer authentication.
- **Header:** `Authorization: Bearer <token>`
- **Token issuance:** A JWT access token is returned on successful login or registration.
- **Token lifetime:** 24 hours (configurable via `application.yml`).
- **Public endpoints** (no token required): home page sections, book catalogue, book detail, author profiles, genre list, review reads.
- **Protected endpoints** (token required): wishlist, cart, checkout, payment, orders, user profile, address management.
- **Role enforcement:** Admin-only endpoints enforce `role = ADMIN` via Spring Security method-level security. Customer endpoints enforce `role = CUSTOMER`.

### 1.5 Standard HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| `200 OK` | Success | Successful GET, PUT, PATCH |
| `201 Created` | Resource created | Successful POST that creates a resource |
| `204 No Content` | Success, no body | Successful DELETE |
| `400 Bad Request` | Invalid input | Validation failures, malformed JSON |
| `401 Unauthorized` | Missing/invalid token | No or expired JWT on protected endpoint |
| `403 Forbidden` | Insufficient permissions | Valid token but wrong role |
| `404 Not Found` | Resource not found | Requested ID does not exist |
| `409 Conflict` | Duplicate resource | Registration with existing email, duplicate review |
| `422 Unprocessable Entity` | Business rule violation | Expired coupon, invalid payment method |
| `500 Internal Server Error` | Unexpected server error | Unhandled exceptions |

### 1.6 Error Handling

All error responses follow a consistent envelope structure:

```json
{
  "success": false,
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "errors": [
    { "field": "email", "message": "must be a valid email address" },
    { "field": "password", "message": "must be at least 8 characters" }
  ],
  "timestamp": "2025-07-21T10:30:00Z",
  "path": "/api/v1/auth/register"
}
```

- `errors` array is present only for `400` validation failures; omitted otherwise.
- `message` provides a human-readable description of the error.
- `error` is a machine-readable error code in SCREAMING_SNAKE_CASE.

### 1.7 Successful Response Envelope

All successful responses are wrapped in a standard envelope:

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

Paginated list responses include pagination metadata:

```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  }
}
```

---

## 2. Authentication APIs

**Base path:** `/api/v1/auth`  
**Authentication required:** None (all auth endpoints are public)

---

### POST `/api/v1/auth/register`

**Description:** Register a new customer account. Returns a JWT token on success so the user is immediately authenticated.

**Request Body:**
```json
{
  "firstName": "Arjun",
  "lastName": "Patel",
  "email": "arjun@example.com",
  "phoneNumber": "+919876543210",
  "password": "SecurePass@123"
}
```

**Validation Rules:**
- `firstName`: required, 1–100 characters
- `lastName`: required, 1–100 characters
- `email`: required, valid email format, must be unique
- `phoneNumber`: optional, valid Indian mobile format (`+91XXXXXXXXXX`)
- `password`: required, 8–64 characters, must contain at least one uppercase letter, one digit, and one special character

**Success Response `201`:**
```json
{
  "success": true,
  "data": {
    "token": "<jwt-access-token>",
    "user": {
      "id": 1,
      "firstName": "Arjun",
      "lastName": "Patel",
      "email": "arjun@example.com",
      "phoneNumber": "+919876543210",
      "role": "CUSTOMER"
    }
  },
  "message": "Registration successful"
}
```

**Error Responses:**
| Status | Error | Condition |
|--------|-------|-----------|
| `400` | `BAD_REQUEST` | Validation failure |
| `409` | `CONFLICT` | Email or phone number already registered |

---

### POST `/api/v1/auth/login`

**Description:** Authenticate an existing user with email/phone and password. Returns a JWT token.

**Request Body:**
```json
{
  "identifier": "arjun@example.com",
  "password": "SecurePass@123"
}
```

**Validation Rules:**
- `identifier`: required — accepts email address or phone number
- `password`: required

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "token": "<jwt-access-token>",
    "user": {
      "id": 1,
      "firstName": "Arjun",
      "lastName": "Patel",
      "email": "arjun@example.com",
      "role": "CUSTOMER"
    }
  },
  "message": "Login successful"
}
```

**Error Responses:**
| Status | Error | Condition |
|--------|-------|-----------|
| `400` | `BAD_REQUEST` | Missing identifier or password |
| `401` | `UNAUTHORIZED` | Invalid credentials |
| `403` | `FORBIDDEN` | Account is deactivated |

---

### POST `/api/v1/auth/logout`

**Description:** Invalidates the current session client-side. Since JWTs are stateless, logout is a client-side token discard. The server acknowledges the request.

**Authentication Required:** Yes

**Success Response `200`:**
```json
{
  "success": true,
  "data": null,
  "message": "Logout successful"
}
```

---

### POST `/api/v1/auth/forgot-password`

**Description:** Initiates password reset by accepting the user's registered email. In Phase 1 the reset token is returned directly in the response (no email service). In Phase 2 the token is sent via email.

**Request Body:**
```json
{
  "email": "arjun@example.com"
}
```

**Validation Rules:**
- `email`: required, valid email format

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "resetToken": "<password-reset-token>",
    "expiresIn": 900
  },
  "message": "Password reset token generated. Token expires in 15 minutes."
}
```

**Error Responses:**
| Status | Error | Condition |
|--------|-------|-----------|
| `400` | `BAD_REQUEST` | Invalid email format |
| `404` | `NOT_FOUND` | Email not registered |

---

### POST `/api/v1/auth/reset-password`

**Description:** Completes the password reset using the token issued by `forgot-password`.

**Request Body:**
```json
{
  "resetToken": "<password-reset-token>",
  "newPassword": "NewSecure@456",
  "confirmPassword": "NewSecure@456"
}
```

**Validation Rules:**
- `resetToken`: required
- `newPassword`: required, 8–64 chars, uppercase + digit + special character
- `confirmPassword`: required, must match `newPassword`

**Success Response `200`:**
```json
{
  "success": true,
  "data": null,
  "message": "Password reset successful"
}
```

**Error Responses:**
| Status | Error | Condition |
|--------|-------|-----------|
| `400` | `BAD_REQUEST` | Validation failure or passwords do not match |
| `422` | `UNPROCESSABLE_ENTITY` | Token expired or already used |

---

## 3. User APIs

**Base path:** `/api/v1/users`  
**Authentication required:** Yes (all user endpoints require a valid JWT)

---

### GET `/api/v1/users/me`

**Description:** Retrieve the authenticated user's profile.

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "Arjun",
    "lastName": "Patel",
    "email": "arjun@example.com",
    "phoneNumber": "+919876543210",
    "role": "CUSTOMER",
    "createdAt": "2025-07-01T09:00:00Z"
  }
}
```

---

### PUT `/api/v1/users/me`

**Description:** Update the authenticated user's profile (name and/or phone number).

**Request Body:**
```json
{
  "firstName": "Arjun",
  "lastName": "Patel",
  "phoneNumber": "+919876543210"
}
```

**Validation Rules:**
- `firstName`: required, 1–100 characters
- `lastName`: required, 1–100 characters
- `phoneNumber`: optional, valid Indian mobile format

**Success Response `200`:** Updated user profile (same shape as GET `/users/me`)

**Error Responses:** `400` validation, `409` phone number already taken

---

### GET `/api/v1/users/me/addresses`

**Description:** Retrieve all saved delivery addresses for the authenticated user.

**Success Response `200`:**
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "firstName": "Arjun",
      "lastName": "Patel",
      "addressLine1": "42 MG Road",
      "addressLine2": "Apt 3B",
      "city": "Bengaluru",
      "state": "Karnataka",
      "country": "India",
      "pinCode": "560001",
      "phoneNumber": "+919876543210",
      "email": "arjun@example.com",
      "isDefault": true,
      "createdAt": "2025-07-01T09:00:00Z"
    }
  ]
}
```

---

### POST `/api/v1/users/me/addresses`

**Description:** Add a new delivery address for the authenticated user.

**Request Body:**
```json
{
  "firstName": "Arjun",
  "lastName": "Patel",
  "addressLine1": "42 MG Road",
  "addressLine2": "Apt 3B",
  "city": "Bengaluru",
  "state": "Karnataka",
  "country": "India",
  "pinCode": "560001",
  "phoneNumber": "+919876543210",
  "email": "arjun@example.com",
  "isDefault": true
}
```

**Validation Rules:**
- `firstName`, `lastName`: required, 1–100 characters
- `addressLine1`: required, max 255 characters
- `addressLine2`: optional, max 255 characters
- `city`, `state`, `country`: required, max 100 characters
- `pinCode`: required, exactly 6 digits (`^\d{6}$`)
- `phoneNumber`: required, valid Indian mobile format
- `email`: required, valid email format

**Success Response `201`:** Full address object

**Error Responses:** `400` validation

---

### PUT `/api/v1/users/me/addresses/{addressId}`

**Description:** Update an existing saved delivery address.

**Path Parameters:**
- `addressId` (Long, required) — ID of the address to update

**Request Body:** Same shape as POST

**Success Response `200`:** Updated address object

**Error Responses:** `400` validation, `404` address not found, `403` address belongs to another user

---

### DELETE `/api/v1/users/me/addresses/{addressId}`

**Description:** Delete a saved delivery address.

**Path Parameters:**
- `addressId` (Long, required)

**Success Response `204`:** No content

**Error Responses:** `404` not found, `403` forbidden

---

### PATCH `/api/v1/users/me/addresses/{addressId}/default`

**Description:** Set an address as the default delivery address. Clears `isDefault` on any previously default address.

**Path Parameters:**
- `addressId` (Long, required)

**Success Response `200`:** Updated address object with `"isDefault": true`

---

## 4. Home APIs

**Base path:** `/api/v1/home`  
**Authentication required:** No (public). Authenticated requests return personalised recommendations.

---

### GET `/api/v1/home`

**Description:** Returns all three home page sections in a single request to minimise round-trips. Recommended section is personalised for authenticated users; falls back to globally popular books for guests.

**Query Parameters:** None

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "recommended": [ /* array of BookSummary */ ],
    "bestsellers": [ /* array of BookSummary */ ],
    "newLaunches":  [ /* array of BookSummary */ ]
  }
}
```

Each `BookSummary` object:
```json
{
  "id": 12,
  "title": "The Joy of Minimalism",
  "author": { "id": 3, "name": "Daniel Reed" },
  "coverImageUrl": "https://cdn.bookstore.com/covers/joy-minimalism.jpg",
  "shortDescription": "Declutter your life to uncover peace, clarity, and joy.",
  "format": "PAPERBACK",
  "genres": [
    { "id": 6, "name": "Non-fiction", "slug": "non-fiction" },
    { "id": 7, "name": "Self Help", "slug": "self-help" }
  ],
  "price": 149.00,
  "estimatedDeliveryDate": "2025-07-28",
  "averageRating": 4.5,
  "copiesSold": 145
}
```

---

### GET `/api/v1/home/recommended`

**Description:** Returns only the Recommended for You section (personalised or editorial fallback). Useful for lazy-loading or refreshing just this section.

**Query Parameters:**
- `limit` (integer, optional, default: `10`, max: `20`) — number of books to return

**Success Response `200`:** `{ "success": true, "data": [ /* BookSummary array */ ] }`

---

### GET `/api/v1/home/bestsellers`

**Description:** Returns the Bestsellers this Month section (ranked by `copies_sold` in the current calendar month).

**Query Parameters:**
- `limit` (integer, optional, default: `10`, max: `20`)

**Success Response `200`:** `{ "success": true, "data": [ /* BookSummary array */ ] }`

---

### GET `/api/v1/home/new-launches`

**Description:** Returns the New Launches section (ranked by `created_at` descending).

**Query Parameters:**
- `limit` (integer, optional, default: `10`, max: `20`)

**Success Response `200`:** `{ "success": true, "data": [ /* BookSummary array */ ] }`

---

## 5. Genre APIs

**Base path:** `/api/v1/genres`  
**Authentication required:** No

---

### GET `/api/v1/genres`

**Description:** Returns the complete list of all genre categories for the sidebar navigation.

**Success Response `200`:**
```json
{
  "success": true,
  "data": [
    { "id": 1, "name": "Romance", "slug": "romance" },
    { "id": 2, "name": "Mystery", "slug": "mystery" },
    { "id": 3, "name": "Science Fiction", "slug": "science-fiction" }
  ]
}
```

---

## 6. Book APIs

**Base path:** `/api/v1/books`  
**Authentication required:** No (all book read endpoints are public)

---

### GET `/api/v1/books`

**Description:** Returns a paginated, filterable, and searchable list of all active books. Powers the catalogue page.

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | integer | No | `0` | Zero-based page index |
| `size` | integer | No | `20` | Page size (max 50) |
| `search` | string | No | — | Full-text search on title, description, author name |
| `genreSlug` | string | No | — | Filter by genre slug (e.g., `science-fiction`) |
| `language` | string | No | — | Filter by language (e.g., `English`) |
| `format` | string | No | — | Filter by format: `PAPERBACK`, `HARDCOVER`, `EBOOK` |
| `minPrice` | number | No | — | Minimum price filter (INR) |
| `maxPrice` | number | No | — | Maximum price filter (INR) |
| `sortBy` | string | No | `relevance` | Sort: `relevance`, `price_asc`, `price_desc`, `newest`, `bestseller` |

**Success Response `200`:** Paginated list of `BookSummary` objects

**Error Responses:** `400` invalid filter values

---

### GET `/api/v1/books/{bookId}`

**Description:** Returns the full detail of a single book, including author bio, publisher, all genres, aggregate rating, and delivery date estimate.

**Path Parameters:**
- `bookId` (Long, required)

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": 12,
    "title": "The Joy of Minimalism",
    "description": "Declutter your life to uncover peace, clarity, and joy...",
    "coverImageUrl": "https://cdn.bookstore.com/covers/joy-minimalism.jpg",
    "isbn": "978-0-000-00000-0",
    "language": "English",
    "format": "PAPERBACK",
    "price": 149.00,
    "copiesSold": 145,
    "publishedDate": "2023-05-15",
    "estimatedDeliveryDate": "2025-07-28",
    "averageRating": 4.5,
    "reviewCount": 32,
    "genres": [
      { "id": 6, "name": "Non-fiction", "slug": "non-fiction" },
      { "id": 7, "name": "Self Help", "slug": "self-help" }
    ],
    "author": {
      "id": 3,
      "name": "Daniel Reed",
      "biography": "Daniel Reed is a writer, minimalist, and productivity coach...",
      "profileImageUrl": "https://cdn.bookstore.com/authors/daniel-reed.jpg"
    },
    "publisher": {
      "id": 2,
      "name": "ABC Publishers",
      "website": "https://abcpublishers.com"
    }
  }
}
```

**Error Responses:** `404` book not found

---

### GET `/api/v1/books/{bookId}/related`

**Description:** Returns books related to the specified book by shared genres. Powers the "Related Reads" sidebar on the detail page.

**Path Parameters:**
- `bookId` (Long, required)

**Query Parameters:**
- `limit` (integer, optional, default: `6`, max: `12`)

**Success Response `200`:** `{ "success": true, "data": [ /* BookSummary array */ ] }`

**Error Responses:** `404` book not found

---

## 7. Author APIs

**Base path:** `/api/v1/authors`  
**Authentication required:** No for profile reads; Yes for follow/unfollow

---

### GET `/api/v1/authors/{authorId}`

**Description:** Returns an author's profile page data including biography and their full book catalogue.

**Path Parameters:**
- `authorId` (Long, required)

**Query Parameters:**
- `page` (integer, optional, default: `0`)
- `size` (integer, optional, default: `20`)

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": 3,
    "name": "Daniel Reed",
    "biography": "Daniel Reed is a writer, minimalist, and productivity coach...",
    "profileImageUrl": "https://cdn.bookstore.com/authors/daniel-reed.jpg",
    "isFollowed": false,
    "books": {
      "content": [ /* BookSummary array */ ],
      "page": 0,
      "size": 20,
      "totalElements": 4,
      "totalPages": 1,
      "last": true
    }
  }
}
```

> `isFollowed` is `false` for unauthenticated requests; reflects actual follow status for authenticated users.

**Error Responses:** `404` author not found

---

### POST `/api/v1/authors/{authorId}/follow`

**Description:** Follow an author. Adds to the user's "My Writers" list.

**Authentication Required:** Yes

**Path Parameters:**
- `authorId` (Long, required)

**Success Response `200`:**
```json
{
  "success": true,
  "data": { "authorId": 3, "followed": true },
  "message": "Author followed successfully"
}
```

**Error Responses:** `404` author not found, `409` already following

---

### DELETE `/api/v1/authors/{authorId}/follow`

**Description:** Unfollow an author. Removes from the user's "My Writers" list.

**Authentication Required:** Yes

**Path Parameters:**
- `authorId` (Long, required)

**Success Response `200`:**
```json
{
  "success": true,
  "data": { "authorId": 3, "followed": false },
  "message": "Author unfollowed successfully"
}
```

**Error Responses:** `404` author not found, `422` not currently following

---

### GET `/api/v1/users/me/followed-authors`

**Description:** Returns all authors the authenticated user follows (My Writers page).

**Authentication Required:** Yes

**Query Parameters:**
- `page` (integer, optional, default: `0`)
- `size` (integer, optional, default: `20`)

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 3,
        "name": "Daniel Reed",
        "biography": "Daniel Reed is a writer...",
        "profileImageUrl": "https://cdn.bookstore.com/authors/daniel-reed.jpg",
        "followedAt": "2025-07-10T14:30:00Z",
        "recentBooks": [ /* BookSummary array (up to 3) */ ]
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 8. Review APIs

**Base path:** `/api/v1/books/{bookId}/reviews`  
**Authentication required:** No for reads; Yes for submitting a review

---

### GET `/api/v1/books/{bookId}/reviews`

**Description:** Returns paginated reviews for a specific book, ordered by most recent.

**Path Parameters:**
- `bookId` (Long, required)

**Query Parameters:**
- `page` (integer, optional, default: `0`)
- `size` (integer, optional, default: `10`)

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "averageRating": 4.5,
    "reviewCount": 32,
    "content": [
      {
        "id": 8,
        "userId": 1,
        "userName": "John Smith",
        "rating": 5,
        "reviewText": "Absolutely life-changing book!",
        "createdAt": "2025-07-15T08:30:00Z"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 32,
    "totalPages": 4,
    "last": false
  }
}
```

**Error Responses:** `404` book not found

---

### POST `/api/v1/books/{bookId}/reviews`

**Description:** Submit a new review for a book. One review per user per book is enforced.

**Authentication Required:** Yes

**Path Parameters:**
- `bookId` (Long, required)

**Request Body:**
```json
{
  "rating": 5,
  "reviewText": "Absolutely life-changing book!"
}
```

**Validation Rules:**
- `rating`: required, integer between 1 and 5
- `reviewText`: optional, max 100 characters

**Success Response `201`:** The created review object

**Error Responses:** `400` validation, `404` book not found, `409` user has already reviewed this book

---

## 9. Wishlist APIs

**Base path:** `/api/v1/wishlist`  
**Authentication required:** Yes (all wishlist endpoints require JWT)

---

### GET `/api/v1/wishlist`

**Description:** Returns all books in the authenticated user's wishlist.

**Query Parameters:**
- `page` (integer, optional, default: `0`)
- `size` (integer, optional, default: `20`)

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "wishlistItemId": 14,
        "addedAt": "2025-07-18T10:00:00Z",
        "book": { /* BookSummary */ }
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1,
    "last": true
  }
}
```

---

### POST `/api/v1/wishlist/items`

**Description:** Add a book to the authenticated user's wishlist.

**Request Body:**
```json
{
  "bookId": 12
}
```

**Validation Rules:**
- `bookId`: required, must reference an active book

**Success Response `201`:**
```json
{
  "success": true,
  "data": { "wishlistItemId": 14, "bookId": 12, "addedAt": "2025-07-21T10:00:00Z" },
  "message": "Book added to wishlist"
}
```

**Error Responses:** `404` book not found, `409` book already in wishlist

---

### DELETE `/api/v1/wishlist/items/{wishlistItemId}`

**Description:** Remove a book from the wishlist.

**Path Parameters:**
- `wishlistItemId` (Long, required)

**Success Response `204`:** No content

**Error Responses:** `404` wishlist item not found

---

### POST `/api/v1/wishlist/items/{wishlistItemId}/move-to-cart`

**Description:** Moves a wishlist item into the shopping cart and removes it from the wishlist.

**Path Parameters:**
- `wishlistItemId` (Long, required)

**Success Response `200`:**
```json
{
  "success": true,
  "data": { "cartItemId": 22, "bookId": 12 },
  "message": "Book moved to cart"
}
```

**Error Responses:** `404` wishlist item not found

---

## 10. Shopping Cart APIs

**Base path:** `/api/v1/cart`  
**Authentication required:** Yes

---

### GET `/api/v1/cart`

**Description:** Returns the authenticated user's current cart with all items and a price summary.

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "cartId": 7,
    "itemCount": 2,
    "items": [
      {
        "cartItemId": 22,
        "bookId": 12,
        "title": "The Joy of Minimalism",
        "coverImageUrl": "https://cdn.bookstore.com/covers/joy-minimalism.jpg",
        "author": { "id": 3, "name": "Daniel Reed" },
        "format": "PAPERBACK",
        "genres": [ { "id": 6, "name": "Non-fiction", "slug": "non-fiction" } ],
        "unitPrice": 149.00,
        "quantity": 1,
        "lineTotal": 149.00,
        "estimatedDeliveryDate": "2025-07-28"
      }
    ],
    "subtotal": 508.00,
    "updatedAt": "2025-07-21T09:45:00Z"
  }
}
```

---

### POST `/api/v1/cart/items`

**Description:** Add a book to the cart. If the book already exists in the cart, its quantity is incremented by the specified amount.

**Request Body:**
```json
{
  "bookId": 12,
  "quantity": 1
}
```

**Validation Rules:**
- `bookId`: required, must reference an active book
- `quantity`: required, integer ≥ 1

**Success Response `201`:**
```json
{
  "success": true,
  "data": { "cartItemId": 22, "bookId": 12, "quantity": 1, "lineTotal": 149.00 },
  "message": "Book added to cart"
}
```

**Error Responses:** `404` book not found, `400` invalid quantity

---

### PUT `/api/v1/cart/items/{cartItemId}`

**Description:** Update the quantity of a cart item. Setting quantity to `0` removes the item.

**Path Parameters:**
- `cartItemId` (Long, required)

**Request Body:**
```json
{
  "quantity": 2
}
```

**Validation Rules:**
- `quantity`: required, integer ≥ 0 (0 = remove item)

**Success Response `200`:**
```json
{
  "success": true,
  "data": { "cartItemId": 22, "bookId": 12, "quantity": 2, "lineTotal": 298.00 },
  "message": "Cart item updated"
}
```

**Error Responses:** `404` cart item not found, `400` invalid quantity

---

### DELETE `/api/v1/cart/items/{cartItemId}`

**Description:** Remove a specific item from the cart.

**Path Parameters:**
- `cartItemId` (Long, required)

**Success Response `204`:** No content

**Error Responses:** `404` cart item not found

---

### DELETE `/api/v1/cart`

**Description:** Clear all items from the cart.

**Success Response `204`:** No content

---

## 11. Checkout APIs

**Base path:** `/api/v1/checkout`  
**Authentication required:** Yes

---

### GET `/api/v1/checkout/summary`

**Description:** Returns a full checkout summary: cart items with quantities, optional applied coupon discount, tax, delivery charge, and grand total. Used to render the checkout page.

**Query Parameters:**
- `couponCode` (string, optional) — coupon to preview before placing order

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "items": [ /* same CartItem shape as GET /cart */ ],
    "subtotal": 508.00,
    "taxAmount": 61.00,
    "deliveryCharge": 0.00,
    "discountAmount": 100.00,
    "totalAmount": 469.00,
    "coupon": {
      "code": "SAVE100",
      "discountType": "FIXED",
      "discountValue": 100.00
    }
  }
}
```

---

### POST `/api/v1/checkout/validate-coupon`

**Description:** Validates a coupon code against the current cart subtotal without placing the order.

**Request Body:**
```json
{
  "couponCode": "SAVE100"
}
```

**Validation Rules:**
- `couponCode`: required, 1–50 characters

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "couponCode": "SAVE100",
    "discountType": "FIXED",
    "discountValue": 100.00,
    "discountAmount": 100.00,
    "valid": true
  },
  "message": "Coupon applied successfully"
}
```

**Error Responses:**
| Status | Error | Condition |
|--------|-------|-----------|
| `404` | `NOT_FOUND` | Coupon code does not exist |
| `422` | `UNPROCESSABLE_ENTITY` | Coupon expired, inactive, or order subtotal below minimum |

---

### POST `/api/v1/checkout/place-order`

**Description:** Creates the order record. Accepts delivery address (inline or saved address ID) and optional coupon code. On success returns the newly created order ready for payment.

**Request Body:**
```json
{
  "couponCode": "SAVE100",
  "deliveryAddress": {
    "savedAddressId": 5
  }
}
```

_Or with an inline address:_
```json
{
  "couponCode": null,
  "deliveryAddress": {
    "firstName": "Arjun",
    "lastName": "Patel",
    "addressLine1": "42 MG Road",
    "addressLine2": "Apt 3B",
    "city": "Bengaluru",
    "state": "Karnataka",
    "country": "India",
    "pinCode": "560001",
    "phoneNumber": "+919876543210",
    "email": "arjun@example.com"
  }
}
```

**Validation Rules:**
- `deliveryAddress`: required — either `savedAddressId` (existing address ID) or full inline address fields
- Inline address fields follow the same rules as POST `/users/me/addresses`
- `couponCode`: optional

**Success Response `201`:**
```json
{
  "success": true,
  "data": {
    "orderId": 7,
    "orderNumber": "BST-20250721-00007",
    "status": "PLACED",
    "subtotal": 508.00,
    "taxAmount": 61.00,
    "deliveryCharge": 0.00,
    "discountAmount": 100.00,
    "totalAmount": 469.00,
    "estimatedDeliveryDate": "2025-07-28",
    "items": [ /* OrderItem array */ ]
  },
  "message": "Order placed successfully. Proceed to payment."
}
```

**Error Responses:**
| Status | Error | Condition |
|--------|-------|-----------|
| `400` | `BAD_REQUEST` | Missing or invalid address fields |
| `404` | `NOT_FOUND` | Saved address ID not found |
| `422` | `UNPROCESSABLE_ENTITY` | Cart is empty, invalid coupon, or coupon expired |

---

## 12. Payment APIs

**Base path:** `/api/v1/payments`  
**Authentication required:** Yes

> **Phase 1 Mock Implementation:** The payment service simulates a payment gateway. Submitting valid payment details always returns a `SUCCESS` status. No real financial transaction is made. Card data is never persisted.

---

### POST `/api/v1/payments/process`

**Description:** Processes payment for a placed order. Accepts the payment method and credentials. On success, marks the order as `CONFIRMED` and clears the user's cart.

**Request Body (Credit Card / Debit Card):**
```json
{
  "orderId": 7,
  "paymentMethod": "CREDIT_CARD",
  "cardDetails": {
    "cardNumber": "<sample-card-number>",
    "nameOnCard": "Arjun Patel",
    "cvv": "123",
    "expiryDate": "12/2027"
  }
}
```

**Request Body (UPI):**
```json
{
  "orderId": 7,
  "paymentMethod": "UPI",
  "upiDetails": {
    "upiId": "arjun@okaxis"
  }
}
```

**Request Body (Wallet):**
```json
{
  "orderId": 7,
  "paymentMethod": "WALLET"
}
```

**Validation Rules:**
- `orderId`: required, must reference a `PLACED` order belonging to the authenticated user
- `paymentMethod`: required, one of `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `WALLET`
- For `CREDIT_CARD` / `DEBIT_CARD`: all `cardDetails` fields required; `cardNumber` 13–19 digits; `cvv` 3–4 digits; `expiryDate` format `MM/YYYY`
- For `UPI`: `upiDetails.upiId` required, valid UPI ID format (`handle@bank`)
- Card data is validated in format only; never persisted

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "paymentId": 4,
    "orderId": 7,
    "orderNumber": "BST-20250721-00007",
    "paymentMethod": "CREDIT_CARD",
    "payableAmount": 469.00,
    "status": "SUCCESS",
    "paidAt": "2025-07-21T10:15:00Z",
    "order": {
      "id": 7,
      "orderNumber": "BST-20250721-00007",
      "status": "CONFIRMED",
      "totalAmount": 469.00,
      "estimatedDeliveryDate": "2025-07-28",
      "items": [
        {
          "bookId": 12,
          "title": "The Joy of Minimalism",
          "coverImageUrl": "https://cdn.bookstore.com/covers/joy-minimalism.jpg",
          "author": { "id": 3, "name": "Daniel Reed" },
          "format": "PAPERBACK",
          "genres": [ { "id": 6, "name": "Non-fiction", "slug": "non-fiction" } ],
          "unitPrice": 149.00,
          "quantity": 1,
          "lineTotal": 149.00,
          "estimatedDeliveryDate": "2025-07-28"
        }
      ]
    }
  },
  "message": "Payment successful. Your order has been confirmed."
}
```

**Error Responses:**
| Status | Error | Condition |
|--------|-------|-----------|
| `400` | `BAD_REQUEST` | Validation failure (missing fields, invalid format) |
| `404` | `NOT_FOUND` | Order not found |
| `422` | `UNPROCESSABLE_ENTITY` | Order not in `PLACED` status, or already paid |

---

### GET `/api/v1/payments/{orderId}`

**Description:** Returns the payment record for a specific order belonging to the authenticated user.

**Path Parameters:**
- `orderId` (Long, required)

**Success Response `200`:** Payment object (same shape as the `data` field above minus the nested `order`)

**Error Responses:** `404` order or payment not found, `403` order belongs to another user

---

## 13. Order APIs

**Base path:** `/api/v1/orders`  
**Authentication required:** Yes

---

### GET `/api/v1/orders`

**Description:** Returns the authenticated user's full purchase history, ordered by most recent first.

**Query Parameters:**
- `page` (integer, optional, default: `0`)
- `size` (integer, optional, default: `10`)
- `status` (string, optional) — filter by status: `PLACED`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 7,
        "orderNumber": "BST-20250721-00007",
        "status": "CONFIRMED",
        "totalAmount": 469.00,
        "itemCount": 2,
        "placedAt": "2025-07-21T10:15:00Z",
        "estimatedDeliveryDate": "2025-07-28",
        "items": [ /* OrderItem summary array */ ]
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 5,
    "totalPages": 1,
    "last": true
  }
}
```

---

### GET `/api/v1/orders/{orderId}`

**Description:** Returns the full details of a single order including all line items, price breakdown, delivery address snapshot, and payment status.

**Path Parameters:**
- `orderId` (Long, required)

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "id": 7,
    "orderNumber": "BST-20250721-00007",
    "status": "CONFIRMED",
    "subtotal": 508.00,
    "taxAmount": 61.00,
    "deliveryCharge": 0.00,
    "discountAmount": 100.00,
    "totalAmount": 469.00,
    "couponCode": "SAVE100",
    "placedAt": "2025-07-21T10:15:00Z",
    "estimatedDeliveryDate": "2025-07-28",
    "deliveryAddress": {
      "firstName": "Arjun",
      "lastName": "Patel",
      "addressLine1": "42 MG Road",
      "city": "Bengaluru",
      "state": "Karnataka",
      "country": "India",
      "pinCode": "560001"
    },
    "payment": {
      "paymentMethod": "CREDIT_CARD",
      "status": "SUCCESS",
      "paidAt": "2025-07-21T10:15:00Z"
    },
    "items": [
      {
        "id": 18,
        "bookId": 12,
        "title": "The Joy of Minimalism",
        "coverImageUrl": "https://cdn.bookstore.com/covers/joy-minimalism.jpg",
        "format": "PAPERBACK",
        "unitPrice": 149.00,
        "quantity": 1,
        "lineTotal": 149.00
      }
    ]
  }
}
```

**Error Responses:** `404` order not found, `403` order belongs to another user

---

### POST `/api/v1/orders/{orderId}/buy-again`

**Description:** Adds all items from a past order back into the current cart. Items already in the cart have their quantity incremented. Returns the updated cart.

**Path Parameters:**
- `orderId` (Long, required)

**Success Response `200`:**
```json
{
  "success": true,
  "data": {
    "cartId": 7,
    "itemCount": 2,
    "itemsAdded": 2,
    "items": [ /* updated CartItem array */ ]
  },
  "message": "2 item(s) added to cart"
}
```

**Error Responses:** `404` order not found, `403` order belongs to another user

---

## API Endpoint Summary

| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 1 | POST | `/api/v1/auth/register` | No | Register new account |
| 2 | POST | `/api/v1/auth/login` | No | Login |
| 3 | POST | `/api/v1/auth/logout` | Yes | Logout |
| 4 | POST | `/api/v1/auth/forgot-password` | No | Request password reset token |
| 5 | POST | `/api/v1/auth/reset-password` | No | Complete password reset |
| 6 | GET | `/api/v1/users/me` | Yes | Get own profile |
| 7 | PUT | `/api/v1/users/me` | Yes | Update own profile |
| 8 | GET | `/api/v1/users/me/addresses` | Yes | List saved addresses |
| 9 | POST | `/api/v1/users/me/addresses` | Yes | Add address |
| 10 | PUT | `/api/v1/users/me/addresses/{id}` | Yes | Update address |
| 11 | DELETE | `/api/v1/users/me/addresses/{id}` | Yes | Delete address |
| 12 | PATCH | `/api/v1/users/me/addresses/{id}/default` | Yes | Set default address |
| 13 | GET | `/api/v1/users/me/followed-authors` | Yes | My Writers list |
| 14 | GET | `/api/v1/home` | No | Full home page |
| 15 | GET | `/api/v1/home/recommended` | No | Recommended books |
| 16 | GET | `/api/v1/home/bestsellers` | No | Bestsellers |
| 17 | GET | `/api/v1/home/new-launches` | No | New Launches |
| 18 | GET | `/api/v1/genres` | No | List all genres |
| 19 | GET | `/api/v1/books` | No | Book catalogue (search + filter) |
| 20 | GET | `/api/v1/books/{id}` | No | Book detail |
| 21 | GET | `/api/v1/books/{id}/related` | No | Related books |
| 22 | GET | `/api/v1/authors/{id}` | No | Author profile |
| 23 | POST | `/api/v1/authors/{id}/follow` | Yes | Follow author |
| 24 | DELETE | `/api/v1/authors/{id}/follow` | Yes | Unfollow author |
| 25 | GET | `/api/v1/books/{id}/reviews` | No | List book reviews |
| 26 | POST | `/api/v1/books/{id}/reviews` | Yes | Submit review |
| 27 | GET | `/api/v1/wishlist` | Yes | Get wishlist |
| 28 | POST | `/api/v1/wishlist/items` | Yes | Add to wishlist |
| 29 | DELETE | `/api/v1/wishlist/items/{id}` | Yes | Remove from wishlist |
| 30 | POST | `/api/v1/wishlist/items/{id}/move-to-cart` | Yes | Move wishlist item to cart |
| 31 | GET | `/api/v1/cart` | Yes | Get cart |
| 32 | POST | `/api/v1/cart/items` | Yes | Add item to cart |
| 33 | PUT | `/api/v1/cart/items/{id}` | Yes | Update cart item quantity |
| 34 | DELETE | `/api/v1/cart/items/{id}` | Yes | Remove cart item |
| 35 | DELETE | `/api/v1/cart` | Yes | Clear cart |
| 36 | GET | `/api/v1/checkout/summary` | Yes | Checkout summary |
| 37 | POST | `/api/v1/checkout/validate-coupon` | Yes | Validate coupon |
| 38 | POST | `/api/v1/checkout/place-order` | Yes | Place order |
| 39 | POST | `/api/v1/payments/process` | Yes | Process payment (mock) |
| 40 | GET | `/api/v1/payments/{orderId}` | Yes | Get payment status |
| 41 | GET | `/api/v1/orders` | Yes | Order history |
| 42 | GET | `/api/v1/orders/{id}` | Yes | Order detail |
| 43 | POST | `/api/v1/orders/{id}/buy-again` | Yes | Buy again |

---

*This document was generated as part of the AI-Assisted Software Development project using IBM Bob.*  
*Document Status: Draft v1.0 — Pending technical review before backend implementation begins.*
