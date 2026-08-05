# Solution Architecture Document
## Book Store — E-Commerce Book Store Application

**Version:** 1.0  
**Date:** July 2025  
**Project Type:** AI-Assisted Full-Stack eCommerce Application  
**Repository:** book-store  
**Based on:** Software Requirements Specification (SRS) v1.0  

---

## Table of Contents

1. [High-Level Solution Architecture](#1-high-level-solution-architecture)
2. [Application Modules and Responsibilities](#2-application-modules-and-responsibilities)
3. [Domain Model](#3-domain-model)
4. [Entity Relationships](#4-entity-relationships)
5. [Backend Package Structure](#5-backend-package-structure)
6. [Frontend Folder Structure](#6-frontend-folder-structure)
7. [Design Principles](#7-design-principles)
8. [Assumptions](#8-assumptions)

---

## 1. High-Level Solution Architecture

The Book Store application follows a classic **three-tier architecture** — a React SPA as the presentation tier, a Spring Boot REST API as the application/business-logic tier, and a PostgreSQL relational database as the data tier. All tiers are decoupled and communicate through well-defined contracts (REST/JSON and JDBC/JPA respectively).

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser)                         │
│                                                                 │
│   ┌─────────────────────────────────────────────────────────┐   │
│   │              React SPA  (Vite + MUI)                    │   │
│   │  Pages · Components · Hooks · Context · Axios Client    │   │
│   └───────────────────────┬─────────────────────────────────┘   │
└───────────────────────────│─────────────────────────────────────┘
                            │  HTTPS  REST / JSON
                            │  JWT Bearer Token (Authorization header)
┌───────────────────────────▼─────────────────────────────────────┐
│                     Spring Boot Application                      │
│                                                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐ │
│  │  Security    │  │  REST        │  │  OpenAPI / Swagger UI  │ │
│  │  Filter      │  │  Controllers │  │  Documentation         │ │
│  │  Chain (JWT) │  │              │  │                        │ │
│  └──────┬───────┘  └──────┬───────┘  └────────────────────────┘ │
│         │                 │                                      │
│  ┌──────▼─────────────────▼───────────────────────────────────┐ │
│  │                   Service Layer                             │ │
│  │   Business Logic · Validation · Transaction Management     │ │
│  └──────────────────────────┬──────────────────────────────── ┘ │
│                              │                                   │
│  ┌───────────────────────────▼──────────────────────────────┐   │
│  │               Repository Layer (Spring Data JPA)         │   │
│  │          JPA Entities · JPQL Queries · Flyway Migrations │   │
│  └───────────────────────────┬──────────────────────────────┘   │
└──────────────────────────────│───────────────────────────────────┘
                               │  JDBC / HikariCP Connection Pool
┌──────────────────────────────▼───────────────────────────────────┐
│                     PostgreSQL 15 Database                        │
│         Tables · Indexes · Full-Text Search · Sequences          │
└──────────────────────────────────────────────────────────────────┘
                               
            ┌──────────────────────────────────────┐
            │    External Components (Phase 2)      │
            │  Payment Gateway (Razorpay / PayU)    │
            │  Logistics / Courier API              │
            │  Email / SMS Notification Service     │
            └──────────────────────────────────────┘
```

---

### 1.1 Frontend

| Concern | Decision |
|---------|----------|
| **Framework** | React 18 with Vite as the build tool and development server. |
| **UI Component Library** | Material UI (MUI) v5 — provides a dark-theme-compatible design system consistent with the Book Store visual design. |
| **Routing** | React Router v6 — declarative client-side routing with nested route layouts for the catalogue, detail, checkout, and account sections. |
| **HTTP Communication** | Axios — configured with a base URL and request interceptors that attach the JWT `Authorization` header for all authenticated calls. |
| **Global State** | React Context API manages authentication state (user identity, JWT token) and cart item count across the entire application. |
| **Server State** | React Query manages remote data fetching, caching, background refresh, and loading/error states for catalogue, book detail, and order data. |
| **Form Handling** | React Hook Form provides performant, uncontrolled form management with inline validation for login, registration, checkout, and review forms. |
| **Deployment Artefact** | `vite build` produces a static bundle (`dist/`) that can be served by any static hosting or embedded in the Spring Boot JAR's `static` resources for single-JAR deployment. |

---

### 1.2 Backend

| Concern | Decision |
|---------|----------|
| **Framework** | Spring Boot 3 (Java 17) — auto-configured, production-ready REST application with embedded Tomcat. |
| **API Style** | RESTful JSON APIs versioned under `/api/v1/`. Public endpoints (catalogue, home, book detail) are unauthenticated. Protected endpoints require a valid JWT. |
| **Security** | Spring Security 6 with a stateless JWT filter chain. A custom `JwtAuthenticationFilter` validates tokens on every request before granting access to protected routes. BCrypt is used for password hashing. |
| **Business Logic** | The service layer is the sole location for business rules, transaction boundaries (`@Transactional`), and orchestration between multiple repositories. |
| **Data Access** | Spring Data JPA repositories backed by Hibernate ORM. Complex catalogue search queries use JPQL or native PostgreSQL full-text search via `nativeQuery`. |
| **Boilerplate Reduction** | Lombok annotations (`@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.) are used on entities and DTOs to eliminate getter/setter/constructor boilerplate. |
| **Schema Management** | Flyway manages versioned database migrations, applied automatically at application startup. |
| **API Documentation** | Springdoc OpenAPI 3 generates a live Swagger UI at `/swagger-ui.html`. |
| **Configuration** | Environment-specific configuration (DB credentials, JWT secret, tax rate, delivery offset days) is externalised via `application.yml` and overridable through environment variables. |

---

### 1.3 Database

| Concern | Decision |
|---------|----------|
| **Engine** | PostgreSQL 15 — ACID-compliant, supports native full-text search (`tsvector`/`tsquery`), JSON columns, and advanced indexing. |
| **Connection Pooling** | HikariCP (Spring Boot default) — pool size configured for the expected concurrent user load. |
| **Search** | PostgreSQL full-text search on book `title`, `description`, and author `name` fields covers Phase 1 search requirements without an additional search engine. |
| **Migrations** | All schema changes are managed exclusively through Flyway migration scripts versioned alongside source code. |

---

### 1.4 External Components

| Component | Phase | Purpose |
|-----------|-------|---------|
| **Payment Gateway** (Razorpay / PayU) | Phase 2 | Processes Credit Card, Debit Card, UPI, and Wallet transactions. In Phase 1 the payment flow is simulated by a mock service. |
| **Email / SMS Service** (SendGrid / Twilio) | Phase 2 | Transactional notifications — order confirmation, shipping updates, password reset OTP. |
| **Logistics / Courier API** | Phase 2 | Live shipment tracking and dynamic delivery date estimation. |
| **Docker / Docker Compose** | Development | Orchestrates the Spring Boot application and PostgreSQL containers for consistent local development environments. |

---

## 2. Application Modules and Responsibilities

| # | Module | Layer Ownership | Responsibility |
|---|--------|----------------|----------------|
| 1 | **Auth & User Management** | Backend + Frontend | User registration, login (email/phone + password), JWT issuance and validation, password reset, user profile, and saved delivery addresses. |
| 2 | **Book Catalogue** | Backend + Frontend | Book inventory metadata, genre-based sidebar navigation, multi-dimensional filtering (language, format, price range, sort order), and full-text keyword search. |
| 3 | **Home Page & Personalisation** | Backend + Frontend | Curated home page sections: Recommended for You (rule-based personalisation), Bestsellers this Month (sales-ranked), and New Launches (recency-ranked). |
| 4 | **Book Detail** | Backend + Frontend | Full book detail view including cover, description, metadata, language, rating, sales count, add-to-cart/wishlist actions, and Related Reads sidebar. |
| 5 | **Author Profiles** | Backend + Frontend | Author bio page with photo, name, biography, and complete list of books in the store. Follow/unfollow action. |
| 6 | **Reviews & Ratings** | Backend + Frontend | Submit and display star ratings and text reviews per book. Aggregate rating computation and display on detail pages. |
| 7 | **Shopping Cart** | Backend + Frontend | Persistent cart for authenticated users (in-memory/session for guests). Item addition, quantity adjustment, item removal, and cart badge count in navigation. |
| 8 | **Checkout & Order Placement** | Backend + Frontend | Checkout flow: cart review, delivery address entry/selection, coupon validation and discount application, price breakdown (subtotal, tax, delivery, discount, total), and final order record creation. |
| 9 | **Payment Processing** | Backend + Frontend | Payment modal with Credit Card, Debit Card, UPI, and Wallet tabs. Simulated payment execution in Phase 1; returns success/failure to trigger order confirmation or error state. |
| 10 | **Order Management** | Backend + Frontend | My Orders page: full purchase history, per-order detail, order status, and Buy Again action to re-add past items to cart. |
| 11 | **Wishlist** | Backend + Frontend | Add/remove books from a persistent personal wishlist. Move-to-cart action. |
| 12 | **My Writers (Author Following)** | Backend + Frontend | Follow/unfollow authors, display followed-author list with their books, surface new releases in recommendations. |
| 13 | **Admin Catalogue Management** | Backend only (Phase 2) | REST APIs for creating/updating/deactivating books, managing authors, configuring coupons, and moderating reviews. No frontend admin UI in Phase 1. |

---

## 3. Domain Model

The following entities form the core domain model of the Book Store application. Each entity encapsulates a distinct business concept.

### 3.1 User
Represents a registered customer or admin. Stores identity credentials, contact information, and role.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique system identifier (UUID or auto-increment Long). |
| `firstName`, `lastName` | Customer display name. |
| `email` | Unique email address used for login and notifications. |
| `phoneNumber` | Unique phone number, alternative login identifier. |
| `passwordHash` | BCrypt-hashed password; plaintext is never stored. |
| `role` | Enumerated role: `CUSTOMER`, `ADMIN`. |
| `createdAt` | Account registration timestamp. |
| `isActive` | Soft-delete / account suspension flag. |

---

### 3.2 Address
Represents a saved delivery address belonging to a User. A User may have multiple addresses with one designated as default.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `userId` | Foreign key to the owning User. |
| `firstName`, `lastName` | Recipient name for delivery. |
| `addressLine1`, `addressLine2` | Street address. |
| `city`, `state`, `country`, `pinCode` | Location fields. |
| `phoneNumber` | Contact number for delivery. |
| `email` | Email for delivery communication. |
| `isDefault` | Boolean flag for the user's default address. |

---

### 3.3 Author
Represents a book author available in the store.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `name` | Author's full display name. |
| `biography` | Long-form author bio displayed on the Author Profile page. |
| `profileImageUrl` | URL of the author's profile photo. |

---

### 3.4 Publisher
Represents a book publisher.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `name` | Publisher name displayed on the book detail page. |
| `website` | Optional publisher website URL. |

---

### 3.5 Genre
Represents a book genre / category (e.g., Romance, Mystery, Science Fiction).

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `name` | Genre display name used in sidebar navigation and book tags. |
| `slug` | URL-safe slug for routing (e.g., `science-fiction`). |

---

### 3.6 Book
The central product entity. Represents a single book title with full metadata.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `title` | Book title. |
| `description` | Full book synopsis/description. |
| `coverImageUrl` | URL of the book cover image. |
| `isbn` | International Standard Book Number. |
| `language` | Language of the book content (e.g., English). |
| `format` | Enumerated format: `PAPERBACK`, `HARDCOVER`, `EBOOK`. |
| `price` | Selling price in INR (BigDecimal for precision). |
| `copiesSold` | Running count of copies sold (used for Bestsellers ranking). |
| `publishedDate` | Original publication date. |
| `createdAt` | Date the record was added to the system (used for New Launches ranking). |
| `isActive` | Whether the book is visible in the catalogue. |
| `authorId` | Foreign key to the book's primary Author. |
| `publisherId` | Foreign key to the book's Publisher. |

---

### 3.7 BookGenre *(Join Entity)*
Resolves the many-to-many relationship between Books and Genres.

| Attribute | Description |
|-----------|-------------|
| `bookId` | Foreign key to Book. |
| `genreId` | Foreign key to Genre. |

---

### 3.8 Review
Represents a customer review (rating + text) for a specific book.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `bookId` | Foreign key to the reviewed Book. |
| `userId` | Foreign key to the reviewing User. |
| `rating` | Integer star rating (1–5). |
| `reviewText` | Free-text review body (max 100 characters per UI constraint). |
| `createdAt` | Submission timestamp. |

---

### 3.9 Cart
Represents an authenticated user's active shopping cart. Each user has at most one active cart at a time.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `userId` | Foreign key to the owning User. |
| `createdAt` | Cart creation timestamp. |
| `updatedAt` | Last modification timestamp. |

---

### 3.10 CartItem
Represents a single book entry (with quantity) inside a Cart.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `cartId` | Foreign key to the parent Cart. |
| `bookId` | Foreign key to the Book being added. |
| `quantity` | Number of copies added to cart. |

---

### 3.11 Coupon
Represents a discount coupon pre-configured by the admin.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `code` | Unique alphanumeric coupon code entered by the customer. |
| `discountType` | Enumerated type: `FIXED` (flat INR discount) or `PERCENTAGE`. |
| `discountValue` | Discount amount or percentage value. |
| `minOrderValue` | Minimum order subtotal required to apply the coupon. |
| `expiresAt` | Coupon expiry date/time. |
| `isActive` | Whether the coupon is currently redeemable. |

---

### 3.12 Order
Represents a confirmed, paid purchase by a Customer. Created atomically when payment succeeds.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique system order identifier. |
| `orderNumber` | Human-readable unique order reference (e.g., `BST-20250721-00042`). |
| `userId` | Foreign key to the purchasing User. |
| `status` | Enumerated status: `PLACED`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`. |
| `subtotal` | Sum of all item prices before tax and discounts. |
| `taxAmount` | Computed tax applied to the subtotal. |
| `deliveryCharge` | Delivery fee (0 for free delivery). |
| `discountAmount` | Coupon discount applied to the order. |
| `totalAmount` | Final payable amount (subtotal + tax + delivery − discount). |
| `couponId` | Foreign key to the applied Coupon (nullable). |
| `deliveryAddressSnapshot` | JSON snapshot of the delivery address at time of order (to preserve historical accuracy even if the user later changes their address). |
| `placedAt` | Order placement timestamp. |
| `estimatedDeliveryDate` | Computed delivery date at time of order placement. |

---

### 3.13 OrderItem
Represents a single book line within an Order.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `orderId` | Foreign key to the parent Order. |
| `bookId` | Foreign key to the purchased Book. |
| `title` | Book title snapshot at time of purchase. |
| `coverImageUrl` | Cover image snapshot at time of purchase. |
| `format` | Format snapshot at time of purchase. |
| `unitPrice` | Price per copy at time of purchase. |
| `quantity` | Number of copies purchased. |
| `lineTotal` | `unitPrice × quantity`. |

---

### 3.14 Payment
Records the payment transaction associated with an Order.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `orderId` | One-to-one foreign key to the Order. |
| `paymentMethod` | Enumerated method: `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `WALLET`. |
| `payableAmount` | Amount charged. |
| `status` | Enumerated status: `PENDING`, `SUCCESS`, `FAILED`. |
| `transactionReference` | Gateway-returned transaction ID (Phase 2; null in Phase 1 mock). |
| `paidAt` | Payment completion timestamp. |

---

### 3.15 Wishlist
Represents a User's collection of saved books for future purchase.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `userId` | Foreign key to the owning User (one wishlist per user). |
| `createdAt` | Wishlist creation timestamp. |

---

### 3.16 WishlistItem
Represents a single book entry in a Wishlist.

| Attribute | Description |
|-----------|-------------|
| `id` | Unique identifier. |
| `wishlistId` | Foreign key to the parent Wishlist. |
| `bookId` | Foreign key to the wishlisted Book. |
| `addedAt` | Timestamp when the item was added. |

---

### 3.17 UserFollowedAuthor *(Join Entity)*
Resolves the many-to-many follow relationship between Users and Authors.

| Attribute | Description |
|-----------|-------------|
| `userId` | Foreign key to the following User. |
| `authorId` | Foreign key to the followed Author. |
| `followedAt` | Timestamp of the follow action. |

---

## 4. Entity Relationships

### 4.1 Relationship Summary Table

| Relationship | Type | Description |
|-------------|------|-------------|
| User → Address | One-to-Many | A User may have multiple saved delivery addresses; each Address belongs to one User. |
| User → Cart | One-to-One | Each User has at most one active Cart at a time. |
| User → Wishlist | One-to-One | Each User has exactly one persistent Wishlist. |
| User → Order | One-to-Many | A User may place many Orders over time; each Order belongs to one User. |
| User → Review | One-to-Many | A User may submit multiple reviews (one per book); each Review belongs to one User. |
| User ↔ Author | Many-to-Many | Users may follow many Authors; an Author may be followed by many Users. Resolved via `UserFollowedAuthor`. |
| Author → Book | One-to-Many | An Author may have written many Books; each Book has one primary Author. |
| Publisher → Book | One-to-Many | A Publisher may publish many Books; each Book belongs to one Publisher. |
| Book ↔ Genre | Many-to-Many | A Book may belong to multiple Genres; a Genre may contain many Books. Resolved via `BookGenre`. |
| Book → Review | One-to-Many | A Book may have many Reviews; each Review targets one Book. |
| Cart → CartItem | One-to-Many | A Cart contains many CartItems; each CartItem belongs to one Cart. |
| CartItem → Book | Many-to-One | Many CartItems may reference the same Book (across different carts). |
| Order → OrderItem | One-to-Many | An Order contains one or more OrderItems; each OrderItem belongs to one Order. |
| OrderItem → Book | Many-to-One | Many OrderItems may reference the same Book (across different orders); price/title are snapshotted to preserve order history integrity. |
| Order → Payment | One-to-One | Each Order has exactly one Payment record; a Payment belongs to one Order. |
| Order → Coupon | Many-to-One | Many Orders may use the same Coupon; each Order may optionally reference one Coupon. |
| Wishlist → WishlistItem | One-to-Many | A Wishlist contains many WishlistItems; each item belongs to one Wishlist. |
| WishlistItem → Book | Many-to-One | Many WishlistItems may reference the same Book (across different wishlists). |

---

### 4.2 Relationship Diagram (Textual ERD)

```
User ─────────────── 1:N ─── Address
User ─────────────── 1:1 ─── Cart ──── 1:N ─── CartItem ─── N:1 ─── Book
User ─────────────── 1:1 ─── Wishlist ─ 1:N ─── WishlistItem ─ N:1 ─── Book
User ─────────────── 1:N ─── Order ─── 1:N ─── OrderItem ─── N:1 ─── Book
                                │
                                ├─── 1:1 ─── Payment
                                └─── N:1 ─── Coupon (optional)

User ─────────────── N:M ─── Author  (via UserFollowedAuthor)
User ─────────────── 1:N ─── Review ─── N:1 ─── Book

Author ──────────── 1:N ─── Book
Publisher ────────── 1:N ─── Book
Book ─────────────── N:M ─── Genre  (via BookGenre)
Book ─────────────── 1:N ─── Review
```

---

## 5. Backend Package Structure

The backend follows a feature-aligned layered package structure rooted under the base package `com.bookstore`.

```
backend/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── bookstore/
        │           │
        │           ├── BookStoreApplication.java          # Spring Boot entry point
        │           │
        │           ├── config/                            # Cross-cutting configuration
        │           │   ├── SecurityConfig.java            # Spring Security filter chain, CORS, public routes
        │           │   ├── JwtConfig.java                 # JWT secret, expiry configuration properties
        │           │   ├── SwaggerConfig.java             # OpenAPI 3 / Springdoc configuration
        │           │   └── WebMvcConfig.java              # MVC configuration, CORS mappings
        │           │
        │           ├── security/                          # Security components
        │           │   ├── JwtTokenProvider.java          # JWT generation, parsing, and validation
        │           │   ├── JwtAuthenticationFilter.java   # Per-request JWT validation filter
        │           │   └── UserPrincipal.java             # Spring Security UserDetails implementation
        │           │
        │           ├── common/                            # Shared utilities and cross-cutting concerns
        │           │   ├── exception/
        │           │   │   ├── GlobalExceptionHandler.java  # @ControllerAdvice for unified error responses
        │           │   │   ├── ResourceNotFoundException.java
        │           │   │   ├── BadRequestException.java
        │           │   │   └── UnauthorizedException.java
        │           │   ├── response/
        │           │   │   ├── ApiResponse.java           # Generic success response wrapper
        │           │   │   └── PagedResponse.java         # Paginated response wrapper
        │           │   └── util/
        │           │       ├── DeliveryDateCalculator.java # Calculates estimated delivery date
        │           │       └── PriceCalculator.java        # Tax and discount computation
        │           │
        │           ├── domain/                            # Domain model (JPA entities)
        │           │   ├── User.java
        │           │   ├── Address.java
        │           │   ├── Author.java
        │           │   ├── Publisher.java
        │           │   ├── Genre.java
        │           │   ├── Book.java
        │           │   ├── BookGenre.java
        │           │   ├── Review.java
        │           │   ├── Cart.java
        │           │   ├── CartItem.java
        │           │   ├── Coupon.java
        │           │   ├── Order.java
        │           │   ├── OrderItem.java
        │           │   ├── Payment.java
        │           │   ├── Wishlist.java
        │           │   ├── WishlistItem.java
        │           │   ├── UserFollowedAuthor.java
        │           │   └── enums/
        │           │       ├── BookFormat.java            # PAPERBACK, HARDCOVER, EBOOK
        │           │       ├── OrderStatus.java           # PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
        │           │       ├── PaymentMethod.java         # CREDIT_CARD, DEBIT_CARD, UPI, WALLET
        │           │       ├── PaymentStatus.java         # PENDING, SUCCESS, FAILED
        │           │       ├── DiscountType.java          # FIXED, PERCENTAGE
        │           │       └── UserRole.java              # CUSTOMER, ADMIN
        │           │
        │           ├── repository/                        # Spring Data JPA repositories
        │           │   ├── UserRepository.java
        │           │   ├── AddressRepository.java
        │           │   ├── AuthorRepository.java
        │           │   ├── PublisherRepository.java
        │           │   ├── GenreRepository.java
        │           │   ├── BookRepository.java
        │           │   ├── ReviewRepository.java
        │           │   ├── CartRepository.java
        │           │   ├── CartItemRepository.java
        │           │   ├── CouponRepository.java
        │           │   ├── OrderRepository.java
        │           │   ├── OrderItemRepository.java
        │           │   ├── PaymentRepository.java
        │           │   ├── WishlistRepository.java
        │           │   └── WishlistItemRepository.java
        │           │
        │           ├── service/                           # Business logic layer
        │           │   ├── AuthService.java               # Registration, login, password reset, JWT issuance
        │           │   ├── UserService.java               # Profile management, saved addresses
        │           │   ├── BookService.java               # Catalogue, search, filtering, detail
        │           │   ├── AuthorService.java             # Author profiles, follow/unfollow
        │           │   ├── GenreService.java              # Genre listing
        │           │   ├── HomeService.java               # Recommended, Bestsellers, New Launches sections
        │           │   ├── ReviewService.java             # Submit and retrieve reviews, aggregate rating
        │           │   ├── CartService.java               # Add/remove/update cart items
        │           │   ├── CheckoutService.java           # Coupon validation, price breakdown, order creation
        │           │   ├── PaymentService.java            # Simulated payment processing, status update
        │           │   ├── OrderService.java              # Order history, order detail, Buy Again
        │           │   └── WishlistService.java           # Add/remove wishlist items, move to cart
        │           │
        │           ├── dto/                               # Data Transfer Objects (request & response)
        │           │   ├── request/
        │           │   │   ├── LoginRequest.java
        │           │   │   ├── RegisterRequest.java
        │           │   │   ├── PasswordResetRequest.java
        │           │   │   ├── AddressRequest.java
        │           │   │   ├── CartItemRequest.java
        │           │   │   ├── CheckoutRequest.java
        │           │   │   ├── CouponApplyRequest.java
        │           │   │   ├── PaymentRequest.java
        │           │   │   └── ReviewRequest.java
        │           │   └── response/
        │           │       ├── AuthResponse.java          # JWT token + user summary on login
        │           │       ├── UserProfileResponse.java
        │           │       ├── BookSummaryResponse.java   # Lightweight card view (list pages)
        │           │       ├── BookDetailResponse.java    # Full detail page view
        │           │       ├── AuthorResponse.java
        │           │       ├── GenreResponse.java
        │           │       ├── HomePageResponse.java      # Aggregates all three home sections
        │           │       ├── ReviewResponse.java
        │           │       ├── CartResponse.java
        │           │       ├── CheckoutSummaryResponse.java
        │           │       ├── OrderResponse.java
        │           │       ├── OrderDetailResponse.java
        │           │       ├── PaymentResponse.java
        │           │       └── WishlistResponse.java
        │           │
        │           └── controller/                        # REST API controllers
        │               ├── AuthController.java            # POST /api/v1/auth/**
        │               ├── UserController.java            # GET|PUT /api/v1/users/**
        │               ├── BookController.java            # GET /api/v1/books/**
        │               ├── AuthorController.java          # GET /api/v1/authors/**
        │               ├── GenreController.java           # GET /api/v1/genres
        │               ├── HomeController.java            # GET /api/v1/home
        │               ├── ReviewController.java          # GET|POST /api/v1/books/{id}/reviews
        │               ├── CartController.java            # GET|POST|PUT|DELETE /api/v1/cart/**
        │               ├── CheckoutController.java        # POST /api/v1/checkout/**
        │               ├── PaymentController.java         # POST /api/v1/payments/**
        │               ├── OrderController.java           # GET /api/v1/orders/**
        │               └── WishlistController.java        # GET|POST|DELETE /api/v1/wishlist/**
        │
        └── resources/
            ├── application.yml                            # Main configuration
            ├── application-dev.yml                        # Development overrides
            ├── application-prod.yml                       # Production overrides
            └── db/
                └── migration/                             # Flyway migration scripts
                    ├── V1__create_users_addresses.sql
                    ├── V2__create_authors_publishers.sql
                    ├── V3__create_genres_books_book_genres.sql
                    ├── V4__create_reviews.sql
                    ├── V5__create_carts_cart_items.sql
                    ├── V6__create_orders_order_items_payments.sql
                    ├── V7__create_wishlists_wishlist_items.sql
                    ├── V8__create_user_followed_authors.sql
                    ├── V9__create_coupons.sql
                    └── V10__seed_genres_and_sample_data.sql
```

---

## 6. Frontend Folder Structure

The frontend is a Vite + React SPA structured by feature, with shared infrastructure in `common/`. This layout scales cleanly as new features are added.

```
frontend/
├── index.html                          # Vite SPA entry point
├── vite.config.js                      # Vite configuration (proxy, aliases)
├── package.json
│
└── src/
    ├── main.jsx                        # React app bootstrap (render + providers)
    ├── App.jsx                         # Root component with route definitions
    │
    ├── assets/                         # Static assets (images, icons, fonts)
    │   └── images/
    │
    ├── common/                         # Shared, reusable infrastructure
    │   ├── components/                 # Globally reusable UI components
    │   │   ├── BookCard/               # Book thumbnail card (catalogue + home sections)
    │   │   │   ├── BookCard.jsx
    │   │   │   └── BookCard.styles.js
    │   │   ├── Navbar/                 # Top navigation bar with cart badge + auth icon
    │   │   ├── GenreSidebar/           # Left genre category sidebar
    │   │   ├── FilterBar/              # Language / Format / Price / Sort controls
    │   │   ├── Breadcrumb/             # Breadcrumb navigation trail
    │   │   ├── StarRating/             # Read-only and interactive star rating component
    │   │   ├── LoadingSpinner/         # Centred loading indicator
    │   │   ├── ErrorMessage/           # Inline and page-level error display
    │   │   ├── ConfirmModal/           # Generic confirmation modal wrapper
    │   │   └── ProtectedRoute/         # Route guard — redirects guests to login
    │   │
    │   ├── hooks/                      # Shared custom React hooks
    │   │   ├── useAuth.js              # Reads from AuthContext; exposes user + isAuthenticated
    │   │   ├── useCart.js              # Reads from CartContext; exposes cart count + actions
    │   │   └── useDebounce.js          # Debounces search input value
    │   │
    │   ├── context/                    # React Context providers
    │   │   ├── AuthContext.jsx         # User identity, JWT token, login/logout actions
    │   │   └── CartContext.jsx         # Cart item count, add/remove actions
    │   │
    │   ├── api/                        # Axios client and all API call functions
    │   │   ├── axiosClient.js          # Axios instance with baseURL + JWT interceptor
    │   │   ├── authApi.js              # login, register, forgotPassword
    │   │   ├── bookApi.js              # getBooks, getBookById, searchBooks, getRelatedBooks
    │   │   ├── authorApi.js            # getAuthorById, followAuthor, unfollowAuthor
    │   │   ├── genreApi.js             # getAllGenres
    │   │   ├── homeApi.js              # getHomeSections (recommended, bestsellers, new launches)
    │   │   ├── reviewApi.js            # getReviews, submitReview
    │   │   ├── cartApi.js              # getCart, addItem, updateItem, removeItem
    │   │   ├── checkoutApi.js          # applyCoupon, getCheckoutSummary, placeOrder
    │   │   ├── paymentApi.js           # processPayment
    │   │   ├── orderApi.js             # getOrders, getOrderById, buyAgain
    │   │   └── wishlistApi.js          # getWishlist, addToWishlist, removeFromWishlist, moveToCart
    │   │
    │   ├── constants/                  # Application-wide constants
    │   │   ├── routes.js               # Route path constants (e.g., ROUTES.HOME, ROUTES.CHECKOUT)
    │   │   └── queryKeys.js            # React Query cache key constants
    │   │
    │   └── utils/                      # Pure utility functions
    │       ├── formatCurrency.js       # Formats a number as ₹1,234.00
    │       ├── formatDate.js           # Formats delivery date strings
    │       └── validators.js           # Email, phone, and form field validators
    │
    ├── features/                       # Feature modules (one folder per domain feature)
    │   │
    │   ├── auth/                       # Authentication
    │   │   ├── LoginModal.jsx          # Login / Register modal with "Continue as Guest"
    │   │   ├── RegisterForm.jsx        # New user registration form
    │   │   ├── ForgotPasswordForm.jsx  # Password reset request form
    │   │   └── useAuthMutations.js     # React Query mutations for login/register/reset
    │   │
    │   ├── home/                       # Home page
    │   │   ├── HomePage.jsx            # Composes all three home sections
    │   │   ├── RecommendedSection.jsx  # "Recommended for You" book grid
    │   │   ├── BestsellersSection.jsx  # "Bestsellers this Month" book grid
    │   │   └── NewLaunchesSection.jsx  # "New Launches" book grid
    │   │
    │   ├── catalogue/                  # Book catalogue and search
    │   │   ├── CataloguePage.jsx       # Layout: GenreSidebar + FilterBar + BookGrid
    │   │   ├── BookGrid.jsx            # Responsive grid of BookCard components
    │   │   └── useCatalogueQuery.js    # React Query hook for filtered/paginated book list
    │   │
    │   ├── bookDetail/                 # Book detail page
    │   │   ├── BookDetailPage.jsx      # Full detail layout (main + RelatedReads sidebar)
    │   │   ├── BookDetailMain.jsx      # Cover, metadata, Add to Cart/Wishlist buttons
    │   │   ├── AuthorBioSection.jsx    # "About the Writer" section
    │   │   ├── ReviewsSection.jsx      # Reviews list + submit review form
    │   │   └── RelatedReadsSidebar.jsx # Related books sidebar panel
    │   │
    │   ├── author/                     # Author profile
    │   │   ├── AuthorProfilePage.jsx   # Author photo, bio, books list, Follow button
    │   │   └── useAuthorQuery.js       # React Query hook for author data
    │   │
    │   ├── cart/                       # Shopping cart and checkout
    │   │   ├── CheckoutPage.jsx        # Cart items + Address form + Grand Total panel
    │   │   ├── CartItemRow.jsx         # Single cart item with quantity controls
    │   │   ├── AddressForm.jsx         # Delivery address form (with saved address toggle)
    │   │   ├── GrandTotalPanel.jsx     # Price breakdown + coupon input + Pay Now button
    │   │   └── useCheckoutMutations.js # React Query mutations for coupon + order placement
    │   │
    │   ├── payment/                    # Payment processing
    │   │   ├── PaymentModal.jsx        # Payment method tabs + form fields + Pay Now
    │   │   ├── CreditCardForm.jsx      # Card number, name, CVV, expiry fields
    │   │   ├── UpiForm.jsx             # UPI ID field
    │   │   ├── WalletForm.jsx          # Wallet balance display + confirm
    │   │   ├── OrderConfirmation.jsx   # Post-payment success screen
    │   │   └── usePaymentMutation.js   # React Query mutation for payment submission
    │   │
    │   ├── orders/                     # Order management
    │   │   ├── MyOrdersPage.jsx        # Order history list
    │   │   ├── OrderCard.jsx           # Single order summary with Buy Again button
    │   │   └── useOrdersQuery.js       # React Query hook for order history
    │   │
    │   ├── wishlist/                   # Wishlist
    │   │   ├── WishlistPage.jsx        # List of wishlisted books
    │   │   ├── WishlistItemCard.jsx    # Single wishlist item with Move to Cart / Remove
    │   │   └── useWishlistQuery.js     # React Query hook for wishlist data
    │   │
    │   └── myWriters/                  # My Writers (followed authors)
    │       ├── MyWritersPage.jsx       # List of followed authors with their books
    │       └── useMyWritersQuery.js    # React Query hook for followed authors
    │
    └── theme/                          # MUI theme configuration
        └── theme.js                    # Custom MUI dark theme (palette, typography, components)
```

---

## 7. Design Principles

### 7.1 Layered Architecture

The backend enforces a strict three-layer architecture with unidirectional dependencies:

```
Controller Layer   ──▶   Service Layer   ──▶   Repository Layer   ──▶   Database
(HTTP / REST)            (Business Logic)       (Data Access)
```

- **Controller Layer** is responsible solely for HTTP concerns: deserialising request payloads, delegating to the service layer, and serialising responses. Controllers contain no business logic.
- **Service Layer** owns all business rules, validation, transaction management (`@Transactional`), and orchestration between multiple repositories. This is the only layer that may call other services or repositories.
- **Repository Layer** is responsible solely for data persistence. It uses Spring Data JPA interfaces and JPQL/native queries. Repositories have no knowledge of HTTP or business logic.

This strict separation ensures that business rules can be unit-tested without an HTTP server or database, and that persistence strategies can evolve without touching business logic.

---

### 7.2 Separation of Concerns

Each layer, module, and component has a single, well-defined responsibility:

- **DTOs vs. Entities:** JPA entities are never exposed directly in API responses. Dedicated request/response DTO classes decouple the API contract from the database schema, preventing over-fetching, protecting internal data (e.g., `passwordHash`), and allowing each to evolve independently.
- **API Client Isolation:** All HTTP calls from the frontend are centralised in `src/common/api/`. No component makes `fetch` or `axios` calls directly — they all consume typed API functions. This means the API contract is changed in one place.
- **Context vs. Server State:** Global UI state (auth session, cart count) lives in React Context. All server data (books, orders, wishlist) is managed by React Query, which handles caching, background refresh, and loading states — keeping components thin and declarative.

---

### 7.3 Reusability

- **BookCard** is a single component used identically across the home page sections (Recommended, Bestsellers, New Launches), catalogue grid, Related Reads sidebar, and search results — avoiding duplication of the most common UI element.
- **GenreSidebar** and **FilterBar** are standalone components composed into `CataloguePage` but independently testable and reusable in any future filtered list view.
- **StarRating** supports both read-only (detail page, order history) and interactive (review submission) modes via a single prop.
- **Backend service utilities** (`DeliveryDateCalculator`, `PriceCalculator`) are stateless utility classes called by any service that needs delivery date or pricing computation, avoiding duplicated logic across `CheckoutService` and `OrderService`.

---

### 7.4 Maintainability

- **Feature-Aligned Frontend Structure:** Each feature module in `src/features/` is self-contained (page, sub-components, queries/mutations). A developer can locate, understand, and modify a feature without reading across many unrelated files.
- **Enum-Driven Domain:** Business states (order status, payment method, book format, etc.) are modelled as Java enums, making invalid states unrepresentable and eliminating magic strings across the codebase.
- **Flyway Migrations:** All schema changes are versioned scripts applied in sequence. There is never ambiguity about the current database state; it is reproducible from scratch at any time.
- **OpenAPI Documentation:** API contracts are auto-generated from controller annotations. Frontend and backend developers have a single source of truth for the API without maintaining a separate document.
- **Centralised Exception Handling:** The `GlobalExceptionHandler` (`@ControllerAdvice`) ensures that all error responses follow a consistent structure (`ApiResponse`), eliminating ad-hoc error handling scattered across controllers.

---

### 7.5 Scalability

- **Stateless Backend:** No session state is stored on the server. JWT tokens carry all authentication context. This allows the Spring Boot application to be scaled horizontally by adding instances behind a load balancer without sticky sessions.
- **HikariCP Connection Pooling:** Database connections are pooled and reused, preventing connection exhaustion under burst load.
- **Pagination:** All catalogue and order history list endpoints return paginated responses (`PagedResponse`) to prevent unbounded result sets being loaded into memory or transferred over the network.
- **React Query Caching:** Catalogue and book detail responses are cached client-side, reducing redundant API calls for users browsing the same pages and lowering backend load.
- **Decoupled Tiers:** Because the frontend is a static SPA and the backend is a stateless REST service, each tier can be independently scaled, deployed, and optimised without affecting the other.

---

## 8. Assumptions

1. **Single Monorepo Layout:** The repository contains two top-level directories: `backend/` (Maven project) and `frontend/` (Vite project). Shared concerns (e.g., Docker Compose, CI configuration) reside at the root level.
2. **JWT Stateless Authentication:** Authentication is fully stateless using short-lived JWT access tokens. Refresh token rotation or Redis-backed session management is out of scope for Phase 1.
3. **Single Active Cart Per User:** The architecture supports exactly one active cart per authenticated user at any time. Guest carts are maintained in browser `sessionStorage` and merged into the user's server-side cart upon login.
4. **Order Address Snapshot:** The delivery address is stored as a JSON snapshot on the Order entity at placement time. This preserves historical order accuracy even if the user modifies their saved addresses later.
5. **OrderItem Price Snapshot:** Unit price, title, and cover image URL are snapshotted on OrderItem at placement time. This ensures order history is accurate even if the book's price or metadata changes in the catalogue.
6. **Simulated Payment:** The `PaymentService` in Phase 1 simulates a synchronous payment call — it always returns `SUCCESS` for valid inputs. The architecture is designed to be swapped for a real gateway integration (Razorpay/PayU) in Phase 2 with no changes to the calling `CheckoutService`.
7. **Tax as a Fixed Rate:** Tax is computed as a fixed percentage configured in `application.yml`. No per-book or per-category tax rules are implemented in Phase 1.
8. **Coupon: Single Use Per Order:** Only one coupon code may be applied per order. Stacking multiple coupons is not supported.
9. **Rule-Based Recommendations:** The `HomeService` computes "Recommended for You" by querying books in the same genres as the user's last N orders. No external ML service is involved.
10. **Flyway Manages Schema Exclusively:** `spring.jpa.hibernate.ddl-auto` is set to `validate` (not `create` or `update`) in all environments. Flyway is the sole mechanism for schema creation and modification.

---

*This document was generated as part of the AI-Assisted Software Development project using IBM Bob.*  
*Document Status: Draft v1.0 — Pending technical review and sign-off before implementation begins.*
