# Database Design Document
## Book Store — E-Commerce Book Store Application

**Version:** 1.0  
**Date:** July 2025  
**Project Type:** AI-Assisted Full-Stack eCommerce Application  
**Repository:** book-store  
**Database Engine:** PostgreSQL 15  
**Based on:** SRS v1.0 · Architecture Document v1.0  

---

## Table of Contents

1. [Database Overview](#1-database-overview)
2. [Entity List](#2-entity-list)
3. [Table Design](#3-table-design)
4. [Relationships](#4-relationships)
5. [Constraints](#5-constraints)
6. [Indexing Strategy](#6-indexing-strategy)
7. [Data Integrity Rules](#7-data-integrity-rules)
8. [Design Decisions](#8-design-decisions)

---

## 1. Database Overview

### 1.1 Purpose

The Book Store database is the authoritative persistence layer for all application data, including user accounts, book catalogue metadata, customer carts, orders, payments, wishlists, reviews, and author-follow relationships. It is the single source of truth that the Spring Boot application layer reads from and writes to exclusively through the Spring Data JPA repository layer.

### 1.2 Design Goals

| Goal | Description |
|------|-------------|
| **Correctness** | All business rules that can be expressed as database constraints are enforced at the data layer, not only in application code. |
| **Referential Integrity** | All foreign key relationships are explicitly declared; no orphaned records are permitted. |
| **Historical Accuracy** | Order and payment records are immutable snapshots. Changes to books or addresses after placement do not alter historical order data. |
| **Query Performance** | Indexes are placed on all foreign keys, commonly filtered columns, and full-text search vectors to meet the sub-second query requirements from the SRS. |
| **Clarity** | Table and column names use clear, lowercase `snake_case` identifiers consistent with PostgreSQL conventions and Hibernate's default naming strategy. |
| **Extensibility** | The schema is designed so Phase 2 features (inventory, returns, notifications) can be added as new tables or columns with minimal disruption to existing tables. |

### 1.3 Normalization Approach

The schema is normalised to **Third Normal Form (3NF)** as the baseline:

- **1NF:** All columns hold atomic values. No repeating groups. Every table has a primary key.
- **2NF:** All non-key columns are fully functionally dependent on the entire primary key (relevant primarily to composite-key join tables `book_genre` and `user_followed_author`).
- **3NF:** No transitive dependencies between non-key columns. For example, publisher information is held in the `publishers` table rather than duplicated on every `books` row.

**Deliberate denormalization** is applied in two places, with justification:
- `orders.delivery_address_snapshot` — a JSONB column storing a point-in-time address copy. This controlled redundancy preserves historical accuracy without requiring a complex temporal address versioning scheme.
- `order_items` columns `title`, `cover_image_url`, `format`, `unit_price` — snapshot values copied from `books` at order placement to ensure order history is immutable regardless of future catalogue changes.

---

## 2. Entity List

| # | Table Name | Domain Entity | Description |
|---|-----------|--------------|-------------|
| 1 | `users` | User | Registered customer and admin accounts. Stores credentials, contact info, and role. |
| 2 | `addresses` | Address | Saved delivery addresses belonging to users. Multiple per user; one designated default. |
| 3 | `authors` | Author | Book authors with biography and profile photo. |
| 4 | `publishers` | Publisher | Book publishers with name and optional website. |
| 5 | `genres` | Genre | Book genre categories used in sidebar navigation and book tags. |
| 6 | `books` | Book | The central product catalogue entity. All book metadata, pricing, and availability. |
| 7 | `book_genres` | BookGenre | Join table resolving the many-to-many relationship between books and genres. |
| 8 | `reviews` | Review | Customer-submitted star ratings and text reviews for books. |
| 9 | `carts` | Cart | An authenticated user's active shopping cart (one per user). |
| 10 | `cart_items` | CartItem | Individual book entries with quantity inside a cart. |
| 11 | `coupons` | Coupon | Admin-configured discount coupon codes redeemable at checkout. |
| 12 | `orders` | Order | Confirmed, paid purchase records. Immutable after placement. |
| 13 | `order_items` | OrderItem | Individual book line items within an order. Price and title are snapshotted at placement. |
| 14 | `payments` | Payment | Payment transaction record associated one-to-one with an order. |
| 15 | `wishlists` | Wishlist | A user's persistent saved-for-later book collection (one per user). |
| 16 | `wishlist_items` | WishlistItem | Individual book entries in a wishlist. |
| 17 | `user_followed_authors` | UserFollowedAuthor | Join table resolving the many-to-many follow relationship between users and authors. |

---

## 3. Table Design

---

### 3.1 `users`

**Description:** Stores all registered user accounts. Each row represents one person with a unique email address and/or phone number. The `role` column differentiates customers from administrators.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `first_name` | `VARCHAR(100)` | NOT NULL | — | — | — | User's first name. |
| `last_name` | `VARCHAR(100)` | NOT NULL | — | — | — | User's last name. |
| `email` | `VARCHAR(255)` | NOT NULL | — | — | — | Unique email. Primary login identifier. |
| `phone_number` | `VARCHAR(20)` | NULL | — | — | — | Unique phone number. Alternative login identifier. |
| `password_hash` | `VARCHAR(255)` | NOT NULL | — | — | — | BCrypt-hashed password. Plaintext never stored. |
| `role` | `VARCHAR(20)` | NOT NULL | — | — | `'CUSTOMER'` | Enumerated role: `CUSTOMER` or `ADMIN`. |
| `is_active` | `BOOLEAN` | NOT NULL | — | — | `TRUE` | Soft-delete and account suspension flag. |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Account creation timestamp (UTC). |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Last profile update timestamp (UTC). |

**Unique Constraints:** `email`, `phone_number` (partial — when not null)  
**Check Constraints:** `role IN ('CUSTOMER', 'ADMIN')`

---

### 3.2 `addresses`

**Description:** Stores saved delivery addresses for users. A user may have multiple addresses; the `is_default` flag identifies the one pre-filled at checkout.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `user_id` | `BIGINT` | NOT NULL | — | `users.id` | — | Owning user. |
| `first_name` | `VARCHAR(100)` | NOT NULL | — | — | — | Delivery recipient first name. |
| `last_name` | `VARCHAR(100)` | NOT NULL | — | — | — | Delivery recipient last name. |
| `address_line1` | `VARCHAR(255)` | NOT NULL | — | — | — | Street address line 1. |
| `address_line2` | `VARCHAR(255)` | NULL | — | — | — | Street address line 2 (optional). |
| `city` | `VARCHAR(100)` | NOT NULL | — | — | — | City name. |
| `state` | `VARCHAR(100)` | NOT NULL | — | — | — | State / province. |
| `country` | `VARCHAR(100)` | NOT NULL | — | — | `'India'` | Country. Defaults to India. |
| `pin_code` | `VARCHAR(10)` | NOT NULL | — | — | — | Postal / PIN code. |
| `phone_number` | `VARCHAR(20)` | NOT NULL | — | — | — | Contact number for delivery. |
| `email` | `VARCHAR(255)` | NOT NULL | — | — | — | Email for delivery communications. |
| `is_default` | `BOOLEAN` | NOT NULL | — | — | `FALSE` | True for the user's default delivery address. |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Record creation timestamp. |

**FK Constraint:** `user_id` → `users.id` ON DELETE CASCADE  
**Check Constraints:** `pin_code ~ '^\d{6}$'` (6-digit Indian PIN validation)

---

### 3.3 `authors`

**Description:** Stores book author profiles. Displayed on book detail pages ("About the Writer") and on dedicated author profile pages. Authors can be followed by users.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `name` | `VARCHAR(255)` | NOT NULL | — | — | — | Author's full display name. |
| `biography` | `TEXT` | NULL | — | — | — | Long-form biographical text shown on the author profile page. |
| `profile_image_url` | `VARCHAR(500)` | NULL | — | — | — | URL of the author's profile photo. |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Record creation timestamp. |

**Unique Constraints:** `name` (prevents duplicate author entries)

---

### 3.4 `publishers`

**Description:** Stores book publisher information. A publisher's name is displayed as a clickable link on the book detail page.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `name` | `VARCHAR(255)` | NOT NULL | — | — | — | Publisher's display name. |
| `website` | `VARCHAR(500)` | NULL | — | — | — | Publisher's website URL (optional). |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Record creation timestamp. |

**Unique Constraints:** `name`

---

### 3.5 `genres`

**Description:** Stores the fixed list of book genre categories displayed in the left sidebar navigation. Books are associated with one or more genres via the `book_genres` join table.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `name` | `VARCHAR(100)` | NOT NULL | — | — | — | Genre display name (e.g., "Science Fiction"). |
| `slug` | `VARCHAR(100)` | NOT NULL | — | — | — | URL-safe routing slug (e.g., `science-fiction`). |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Record creation timestamp. |

**Unique Constraints:** `name`, `slug`

---

### 3.6 `books`

**Description:** The central product entity. Each row represents a unique book title (a specific format edition). All catalogue browsing, search, filtering, and product detail pages are driven from this table.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `title` | `VARCHAR(500)` | NOT NULL | — | — | — | Full book title. |
| `description` | `TEXT` | NULL | — | — | — | Book synopsis / blurb. |
| `cover_image_url` | `VARCHAR(500)` | NULL | — | — | — | URL of the book cover image. |
| `isbn` | `VARCHAR(20)` | NULL | — | — | — | ISBN-13 identifier. |
| `language` | `VARCHAR(50)` | NOT NULL | — | — | `'English'` | Content language (e.g., "English", "Hindi"). |
| `format` | `VARCHAR(20)` | NOT NULL | — | — | — | Enumerated: `PAPERBACK`, `HARDCOVER`, `EBOOK`. |
| `price` | `NUMERIC(10,2)` | NOT NULL | — | — | — | Current selling price in INR. |
| `copies_sold` | `INTEGER` | NOT NULL | — | — | `0` | Running total of units sold. Used for Bestsellers ranking. |
| `published_date` | `DATE` | NULL | — | — | — | Original publication date. |
| `is_active` | `BOOLEAN` | NOT NULL | — | — | `TRUE` | Visibility flag. Inactive books are hidden from catalogue. |
| `author_id` | `BIGINT` | NOT NULL | — | `authors.id` | — | Primary author of the book. |
| `publisher_id` | `BIGINT` | NULL | — | `publishers.id` | — | Publisher of this edition (optional). |
| `search_vector` | `TSVECTOR` | NULL | — | — | — | Pre-computed full-text search vector (title + description + author name). |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Record insertion timestamp. Used for New Launches ranking. |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Last catalogue update timestamp. |

**FK Constraints:**  
- `author_id` → `authors.id` ON DELETE RESTRICT  
- `publisher_id` → `publishers.id` ON DELETE SET NULL  

**Unique Constraints:** `isbn` (partial — when not null)  
**Check Constraints:**  
- `format IN ('PAPERBACK', 'HARDCOVER', 'EBOOK')`  
- `price > 0`  
- `copies_sold >= 0`

---

### 3.7 `book_genres`

**Description:** Join table resolving the many-to-many relationship between books and genres. A book may belong to multiple genres (e.g., both "Fiction" and "Thriller"); a genre contains many books.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `book_id` | `BIGINT` | NOT NULL | ✓ (composite) | `books.id` | — | References the book. |
| `genre_id` | `BIGINT` | NOT NULL | ✓ (composite) | `genres.id` | — | References the genre. |

**Primary Key:** Composite (`book_id`, `genre_id`)  
**FK Constraints:**  
- `book_id` → `books.id` ON DELETE CASCADE  
- `genre_id` → `genres.id` ON DELETE CASCADE

---

### 3.8 `reviews`

**Description:** Stores customer-submitted star ratings and text reviews for books. Aggregate ratings displayed on the book detail page are computed from this table. One review per user per book is enforced by a unique constraint.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `book_id` | `BIGINT` | NOT NULL | — | `books.id` | — | The book being reviewed. |
| `user_id` | `BIGINT` | NOT NULL | — | `users.id` | — | The user who wrote the review. |
| `rating` | `SMALLINT` | NOT NULL | — | — | — | Star rating from 1 to 5. |
| `review_text` | `VARCHAR(100)` | NULL | — | — | — | Optional review text body (max 100 characters per UI). |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Review submission timestamp. |

**FK Constraints:**  
- `book_id` → `books.id` ON DELETE CASCADE  
- `user_id` → `users.id` ON DELETE CASCADE  

**Unique Constraints:** (`book_id`, `user_id`) — one review per user per book  
**Check Constraints:** `rating BETWEEN 1 AND 5`

---

### 3.9 `carts`

**Description:** Represents an authenticated user's active shopping cart. Exactly one cart row exists per user. Cart items are managed separately in `cart_items`. Guest carts are managed client-side and merged on login.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `user_id` | `BIGINT` | NOT NULL | — | `users.id` | — | Owning user (one cart per user). |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Cart creation timestamp. |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Last modification timestamp. |

**FK Constraints:** `user_id` → `users.id` ON DELETE CASCADE  
**Unique Constraints:** `user_id` — enforces one active cart per user

---

### 3.10 `cart_items`

**Description:** Individual book entries inside a user's cart. Each row represents one book title added to the cart, with an associated quantity. The same book cannot appear twice in the same cart (duplicate adds increment `quantity`).

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `cart_id` | `BIGINT` | NOT NULL | — | `carts.id` | — | Parent cart. |
| `book_id` | `BIGINT` | NOT NULL | — | `books.id` | — | The book added to cart. |
| `quantity` | `SMALLINT` | NOT NULL | — | — | `1` | Number of copies. Incremented on re-add. |
| `added_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | When the item was first added. |

**FK Constraints:**  
- `cart_id` → `carts.id` ON DELETE CASCADE  
- `book_id` → `books.id` ON DELETE CASCADE  

**Unique Constraints:** (`cart_id`, `book_id`) — prevents duplicate entries for the same book in one cart  
**Check Constraints:** `quantity >= 1`

---

### 3.11 `coupons`

**Description:** Admin-configured discount coupon codes. Customers enter a coupon code at checkout; the application validates the code against this table before applying the discount.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `code` | `VARCHAR(50)` | NOT NULL | — | — | — | Unique alphanumeric coupon code (case-insensitive). |
| `discount_type` | `VARCHAR(20)` | NOT NULL | — | — | — | Enumerated: `FIXED` (flat INR off) or `PERCENTAGE`. |
| `discount_value` | `NUMERIC(8,2)` | NOT NULL | — | — | — | Amount off (INR) or percentage value. |
| `min_order_value` | `NUMERIC(10,2)` | NOT NULL | — | — | `0.00` | Minimum subtotal to qualify for the coupon. |
| `expires_at` | `TIMESTAMPTZ` | NOT NULL | — | — | — | Coupon expiry date and time (UTC). |
| `is_active` | `BOOLEAN` | NOT NULL | — | — | `TRUE` | Whether the coupon is currently redeemable. |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Record creation timestamp. |

**Unique Constraints:** `UPPER(code)` — case-insensitive unique code  
**Check Constraints:**  
- `discount_type IN ('FIXED', 'PERCENTAGE')`  
- `discount_value > 0`  
- `min_order_value >= 0`  
- `discount_value <= 100` when `discount_type = 'PERCENTAGE'`

---

### 3.12 `orders`

**Description:** Represents a confirmed, paid customer purchase. Created atomically upon successful payment. The record is immutable after placement — status updates are the only permitted mutations. The `delivery_address_snapshot` stores the exact address used at order time.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `order_number` | `VARCHAR(30)` | NOT NULL | — | — | — | Human-readable reference (e.g., `BST-20250721-00042`). |
| `user_id` | `BIGINT` | NOT NULL | — | `users.id` | — | The purchasing customer. |
| `status` | `VARCHAR(20)` | NOT NULL | — | — | `'PLACED'` | Enumerated: `PLACED`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`. |
| `subtotal` | `NUMERIC(10,2)` | NOT NULL | — | — | — | Sum of all line totals before tax and discounts. |
| `tax_amount` | `NUMERIC(10,2)` | NOT NULL | — | — | `0.00` | Tax computed at fixed rate on subtotal. |
| `delivery_charge` | `NUMERIC(10,2)` | NOT NULL | — | — | `0.00` | Delivery fee. Zero for free delivery. |
| `discount_amount` | `NUMERIC(10,2)` | NOT NULL | — | — | `0.00` | Coupon discount applied. Zero if no coupon used. |
| `total_amount` | `NUMERIC(10,2)` | NOT NULL | — | — | — | Final charged amount: `subtotal + tax + delivery − discount`. |
| `coupon_id` | `BIGINT` | NULL | — | `coupons.id` | — | Applied coupon (nullable if none used). |
| `delivery_address_snapshot` | `JSONB` | NOT NULL | — | — | — | Immutable JSON snapshot of the delivery address at order placement. |
| `placed_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Order placement timestamp (UTC). |
| `estimated_delivery_date` | `DATE` | NOT NULL | — | — | — | Computed delivery date at placement time. |

**FK Constraints:**  
- `user_id` → `users.id` ON DELETE RESTRICT  
- `coupon_id` → `coupons.id` ON DELETE SET NULL  

**Unique Constraints:** `order_number`  
**Check Constraints:**  
- `status IN ('PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')`  
- `subtotal >= 0`  
- `tax_amount >= 0`  
- `delivery_charge >= 0`  
- `discount_amount >= 0`  
- `total_amount >= 0`

---

### 3.13 `order_items`

**Description:** Individual book line items within a placed order. Book title, cover image URL, format, and unit price are snapshotted at the moment of order placement to ensure the order history reflects what the customer actually purchased and paid, regardless of future catalogue changes.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `order_id` | `BIGINT` | NOT NULL | — | `orders.id` | — | Parent order. |
| `book_id` | `BIGINT` | NOT NULL | — | `books.id` | — | Reference to the source book (for "Buy Again" linking). |
| `title` | `VARCHAR(500)` | NOT NULL | — | — | — | Book title snapshot at time of purchase. |
| `cover_image_url` | `VARCHAR(500)` | NULL | — | — | — | Cover image URL snapshot at time of purchase. |
| `format` | `VARCHAR(20)` | NOT NULL | — | — | — | Book format snapshot: `PAPERBACK`, `HARDCOVER`, or `EBOOK`. |
| `unit_price` | `NUMERIC(10,2)` | NOT NULL | — | — | — | Price per copy at time of purchase (INR). |
| `quantity` | `SMALLINT` | NOT NULL | — | — | — | Number of copies purchased. |
| `line_total` | `NUMERIC(10,2)` | NOT NULL | — | — | — | Computed: `unit_price × quantity`. |

**FK Constraints:**  
- `order_id` → `orders.id` ON DELETE CASCADE  
- `book_id` → `books.id` ON DELETE RESTRICT  

**Check Constraints:**  
- `unit_price > 0`  
- `quantity >= 1`  
- `line_total > 0`

---

### 3.14 `payments`

**Description:** Records the payment transaction linked one-to-one to a confirmed order. In Phase 1 the `transaction_reference` will be null (simulated payment). In Phase 2 it will store the gateway-returned transaction ID from Razorpay/PayU.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `order_id` | `BIGINT` | NOT NULL | — | `orders.id` | — | The associated order (unique — one payment per order). |
| `payment_method` | `VARCHAR(20)` | NOT NULL | — | — | — | Enumerated: `CREDIT_CARD`, `DEBIT_CARD`, `UPI`, `WALLET`. |
| `payable_amount` | `NUMERIC(10,2)` | NOT NULL | — | — | — | Amount charged in INR. Must equal `orders.total_amount`. |
| `status` | `VARCHAR(20)` | NOT NULL | — | — | `'PENDING'` | Enumerated: `PENDING`, `SUCCESS`, `FAILED`. |
| `transaction_reference` | `VARCHAR(255)` | NULL | — | — | — | External gateway transaction ID (Phase 2). |
| `paid_at` | `TIMESTAMPTZ` | NULL | — | — | — | Timestamp of successful payment. NULL until payment succeeds. |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Payment record creation timestamp. |

**FK Constraints:** `order_id` → `orders.id` ON DELETE RESTRICT  
**Unique Constraints:** `order_id` — enforces one payment record per order  
**Check Constraints:**  
- `payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'WALLET')`  
- `status IN ('PENDING', 'SUCCESS', 'FAILED')`  
- `payable_amount > 0`

---

### 3.15 `wishlists`

**Description:** Represents a user's persistent saved-for-later book collection. One wishlist row exists per registered user, created automatically at registration. Wishlist items are stored in `wishlist_items`.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `user_id` | `BIGINT` | NOT NULL | — | `users.id` | — | Owning user (one wishlist per user). |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Wishlist creation timestamp. |

**FK Constraints:** `user_id` → `users.id` ON DELETE CASCADE  
**Unique Constraints:** `user_id` — one wishlist per user

---

### 3.16 `wishlist_items`

**Description:** Individual book entries in a user's wishlist. The same book cannot appear twice in the same wishlist.

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `id` | `BIGSERIAL` | NOT NULL | ✓ | — | auto | Surrogate primary key. |
| `wishlist_id` | `BIGINT` | NOT NULL | — | `wishlists.id` | — | Parent wishlist. |
| `book_id` | `BIGINT` | NOT NULL | — | `books.id` | — | The wishlisted book. |
| `added_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Timestamp when the book was added to the wishlist. |

**FK Constraints:**  
- `wishlist_id` → `wishlists.id` ON DELETE CASCADE  
- `book_id` → `books.id` ON DELETE CASCADE  

**Unique Constraints:** (`wishlist_id`, `book_id`) — prevents duplicate entries

---

### 3.17 `user_followed_authors`

**Description:** Join table implementing the many-to-many follow relationship between users and authors. Used to power the "My Writers" page and to surface new releases from followed authors in "Recommended for You".

| Column | Data Type | Nullable | PK | FK | Default | Description |
|--------|-----------|----------|----|----|---------|-------------|
| `user_id` | `BIGINT` | NOT NULL | ✓ (composite) | `users.id` | — | The following user. |
| `author_id` | `BIGINT` | NOT NULL | ✓ (composite) | `authors.id` | — | The followed author. |
| `followed_at` | `TIMESTAMPTZ` | NOT NULL | — | — | `NOW()` | Timestamp of the follow action. |

**Primary Key:** Composite (`user_id`, `author_id`)  
**FK Constraints:**  
- `user_id` → `users.id` ON DELETE CASCADE  
- `author_id` → `authors.id` ON DELETE CASCADE

---

## 4. Relationships

### 4.1 One-to-One Relationships

| Parent Table | Child Table | Join Column | Description |
|-------------|-------------|-------------|-------------|
| `users` | `carts` | `carts.user_id` | Each user has exactly one active cart. Enforced by a `UNIQUE` constraint on `carts.user_id`. The cart row is created when the user first adds an item. |
| `users` | `wishlists` | `wishlists.user_id` | Each user has exactly one persistent wishlist. Enforced by `UNIQUE` on `wishlists.user_id`. Created automatically at registration. |
| `orders` | `payments` | `payments.order_id` | Each order has exactly one payment record. Enforced by `UNIQUE` on `payments.order_id`. The payment record is created in the same transaction as the order. |

---

### 4.2 One-to-Many Relationships

| Parent Table | Child Table | Join Column | Cardinality Description |
|-------------|-------------|-------------|------------------------|
| `users` | `addresses` | `addresses.user_id` | A user may save multiple delivery addresses. Each address belongs to exactly one user. Cascade delete removes all addresses when a user is deleted. |
| `users` | `orders` | `orders.user_id` | A user may place many orders over time. Each order is owned by one user. DELETE RESTRICT prevents user deletion while orders exist. |
| `users` | `reviews` | `reviews.user_id` | A user may write many reviews (at most one per book). Cascade delete removes the user's reviews on account deletion. |
| `authors` | `books` | `books.author_id` | An author may have written many books in the catalogue. Each book has one primary author. DELETE RESTRICT prevents removing an author with books in the catalogue. |
| `publishers` | `books` | `books.publisher_id` | A publisher may have published many books. Each book optionally references one publisher. SET NULL on publisher deletion preserves the book record. |
| `carts` | `cart_items` | `cart_items.cart_id` | A cart contains one or more items. Cascade delete removes all items when the cart is deleted. |
| `wishlists` | `wishlist_items` | `wishlist_items.wishlist_id` | A wishlist contains zero or more book items. Cascade delete removes all items when the wishlist is deleted. |
| `orders` | `order_items` | `order_items.order_id` | An order contains one or more line items. Cascade delete removes all items if the order is deleted (administrative use only). |
| `books` | `reviews` | `reviews.book_id` | A book may have many reviews. Cascade delete removes reviews if a book is removed from the catalogue. |
| `books` | `cart_items` | `cart_items.book_id` | A book may appear in many users' carts. |
| `books` | `wishlist_items` | `wishlist_items.book_id` | A book may appear in many users' wishlists. |
| `books` | `order_items` | `order_items.book_id` | A book may appear in many historical order lines. DELETE RESTRICT preserves order history. |
| `coupons` | `orders` | `orders.coupon_id` | A coupon may be applied to many orders. SET NULL if a coupon is deleted preserves order records. |

---

### 4.3 Many-to-Many Relationships

| Entity A | Entity B | Join Table | Description |
|----------|----------|-----------|-------------|
| `books` | `genres` | `book_genres` | A book may belong to multiple genres (e.g., "Fiction" and "Thriller"). A genre contains many books. The composite PK `(book_id, genre_id)` prevents duplicate associations. |
| `users` | `authors` | `user_followed_authors` | A user may follow many authors. An author may be followed by many users. The composite PK `(user_id, author_id)` prevents duplicate follow entries. The `followed_at` timestamp supports ordering the My Writers list by recency. |

---

## 5. Constraints

### 5.1 Primary Key Summary

All tables use a `BIGSERIAL` surrogate primary key named `id`, with the exception of two pure join tables that use composite primary keys:

| Table | Primary Key |
|-------|------------|
| All other tables | `id` (BIGSERIAL, auto-increment) |
| `book_genres` | Composite: (`book_id`, `genre_id`) |
| `user_followed_authors` | Composite: (`user_id`, `author_id`) |

---

### 5.2 Foreign Key Summary

| Table | Column | References | On Delete |
|-------|--------|-----------|-----------|
| `addresses` | `user_id` | `users.id` | CASCADE |
| `books` | `author_id` | `authors.id` | RESTRICT |
| `books` | `publisher_id` | `publishers.id` | SET NULL |
| `book_genres` | `book_id` | `books.id` | CASCADE |
| `book_genres` | `genre_id` | `genres.id` | CASCADE |
| `reviews` | `book_id` | `books.id` | CASCADE |
| `reviews` | `user_id` | `users.id` | CASCADE |
| `carts` | `user_id` | `users.id` | CASCADE |
| `cart_items` | `cart_id` | `carts.id` | CASCADE |
| `cart_items` | `book_id` | `books.id` | CASCADE |
| `orders` | `user_id` | `users.id` | RESTRICT |
| `orders` | `coupon_id` | `coupons.id` | SET NULL |
| `order_items` | `order_id` | `orders.id` | CASCADE |
| `order_items` | `book_id` | `books.id` | RESTRICT |
| `payments` | `order_id` | `orders.id` | RESTRICT |
| `wishlists` | `user_id` | `users.id` | CASCADE |
| `wishlist_items` | `wishlist_id` | `wishlists.id` | CASCADE |
| `wishlist_items` | `book_id` | `books.id` | CASCADE |
| `user_followed_authors` | `user_id` | `users.id` | CASCADE |
| `user_followed_authors` | `author_id` | `authors.id` | CASCADE |

---

### 5.3 Unique Constraints Summary

| Table | Columns | Purpose |
|-------|---------|---------|
| `users` | `email` | No two accounts share an email address. |
| `users` | `phone_number` | No two accounts share a phone number (partial — where not null). |
| `authors` | `name` | Prevents duplicate author entries. |
| `publishers` | `name` | Prevents duplicate publisher entries. |
| `genres` | `name` | Genre names must be unique. |
| `genres` | `slug` | URL slugs must be unique. |
| `books` | `isbn` | ISBNs are globally unique (partial — where not null). |
| `reviews` | (`book_id`, `user_id`) | One review per user per book. |
| `carts` | `user_id` | One active cart per user. |
| `cart_items` | (`cart_id`, `book_id`) | A book appears at most once per cart (quantity handles multiples). |
| `coupons` | `UPPER(code)` | Case-insensitive unique coupon codes. |
| `orders` | `order_number` | Human-readable order references are globally unique. |
| `payments` | `order_id` | Exactly one payment record per order. |
| `wishlists` | `user_id` | One wishlist per user. |
| `wishlist_items` | (`wishlist_id`, `book_id`) | A book appears at most once per wishlist. |

---

### 5.4 Check Constraints Summary

| Table | Constraint | Rule |
|-------|-----------|------|
| `users` | `chk_users_role` | `role IN ('CUSTOMER', 'ADMIN')` |
| `addresses` | `chk_pin_code_format` | `pin_code ~ '^\d{6}$'` |
| `books` | `chk_books_format` | `format IN ('PAPERBACK', 'HARDCOVER', 'EBOOK')` |
| `books` | `chk_books_price_positive` | `price > 0` |
| `books` | `chk_books_copies_non_negative` | `copies_sold >= 0` |
| `reviews` | `chk_reviews_rating_range` | `rating BETWEEN 1 AND 5` |
| `cart_items` | `chk_cart_items_qty_positive` | `quantity >= 1` |
| `coupons` | `chk_coupons_type` | `discount_type IN ('FIXED', 'PERCENTAGE')` |
| `coupons` | `chk_coupons_value_positive` | `discount_value > 0` |
| `coupons` | `chk_coupons_min_order` | `min_order_value >= 0` |
| `coupons` | `chk_coupons_pct_max` | `discount_value <= 100 WHERE discount_type = 'PERCENTAGE'` |
| `orders` | `chk_orders_status` | `status IN ('PLACED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')` |
| `orders` | `chk_orders_amounts_non_negative` | `subtotal >= 0 AND tax_amount >= 0 AND delivery_charge >= 0 AND discount_amount >= 0 AND total_amount >= 0` |
| `order_items` | `chk_order_items_format` | `format IN ('PAPERBACK', 'HARDCOVER', 'EBOOK')` |
| `order_items` | `chk_order_items_unit_price` | `unit_price > 0` |
| `order_items` | `chk_order_items_qty_positive` | `quantity >= 1` |
| `payments` | `chk_payments_method` | `payment_method IN ('CREDIT_CARD','DEBIT_CARD','UPI','WALLET')` |
| `payments` | `chk_payments_status` | `status IN ('PENDING','SUCCESS','FAILED')` |
| `payments` | `chk_payments_amount_positive` | `payable_amount > 0` |

---

### 5.5 Not Null Constraints

All columns marked **NOT NULL** in Section 3 enforce the not-null constraint. Key business rules upheld by NOT NULL:

- A `users` row always has a `first_name`, `last_name`, `email`, `password_hash`, and `role`.
- A `books` row always has a `title`, `price`, `format`, `language`, and `author_id`.
- An `orders` row always has a `user_id`, `total_amount`, `status`, `delivery_address_snapshot`, and `placed_at`.
- An `order_items` row always has all snapshot columns (`title`, `format`, `unit_price`, `quantity`, `line_total`).
- A `payments` row always has an `order_id`, `payment_method`, `payable_amount`, and `status`.

---

## 6. Indexing Strategy

Indexes are placed on columns that are routinely used in `WHERE`, `JOIN`, `ORDER BY`, and full-text search operations to meet the sub-second query performance targets from the SRS.

### 6.1 Primary Key Indexes (Automatic)

PostgreSQL automatically creates a B-tree index on every primary key column. These cover all `id`-based lookups, including JPA repository `findById()` calls.

---

### 6.2 Foreign Key Indexes

PostgreSQL does **not** automatically index foreign key columns. All FK columns are explicitly indexed to avoid sequential scans during JOIN operations.

| Table | Index Column(s) | Rationale |
|-------|----------------|-----------|
| `addresses` | `user_id` | Fetch all addresses for a user (profile page, checkout). |
| `books` | `author_id` | Fetch all books by an author (author profile page). |
| `books` | `publisher_id` | Fetch all books by a publisher. |
| `book_genres` | `genre_id` | Fetch all books in a genre (genre filter — highest frequency query). |
| `reviews` | `book_id` | Fetch all reviews for a book (book detail page). |
| `reviews` | `user_id` | Fetch all reviews by a user (profile / moderation). |
| `cart_items` | `cart_id` | Fetch all items in a cart. |
| `cart_items` | `book_id` | Check if a specific book is already in a cart. |
| `orders` | `user_id` | Fetch all orders for a user (My Orders page). |
| `order_items` | `order_id` | Fetch all items in a specific order. |
| `order_items` | `book_id` | Support Buy Again — check if a book was previously ordered. |
| `payments` | `order_id` | Fetch payment status for an order (covered by unique constraint index). |
| `wishlist_items` | `wishlist_id` | Fetch all items in a wishlist. |
| `wishlist_items` | `book_id` | Check if a book is already wishlisted. |
| `user_followed_authors` | `author_id` | Fetch all followers of an author. |

---

### 6.3 Query Performance Indexes

Indexes on columns used in catalogue filtering, sorting, and business logic queries.

| Table | Index Column(s) | Index Type | Rationale |
|-------|----------------|-----------|-----------|
| `users` | `email` | B-tree (UNIQUE) | Login by email — executed on every login attempt. |
| `users` | `phone_number` | B-tree (partial UNIQUE) | Login by phone number. |
| `books` | `is_active` | B-tree (partial) | All catalogue queries filter `WHERE is_active = TRUE`. |
| `books` | `format` | B-tree | Filter by book format (Paperback / eBook / Hardcover). |
| `books` | `language` | B-tree | Filter by language. |
| `books` | `price` | B-tree | Price range filter and price sort (`ORDER BY price`). |
| `books` | `copies_sold DESC` | B-tree | Bestsellers ranking — ordered by highest sales. |
| `books` | `created_at DESC` | B-tree | New Launches ranking — ordered by most recent addition. |
| `genres` | `slug` | B-tree (UNIQUE) | Genre lookup by URL slug on every catalogue navigation. |
| `coupons` | `UPPER(code)` | B-tree (UNIQUE) | Coupon validation at checkout by code. |
| `orders` | `placed_at DESC` | B-tree | Order history ordered by most recent first. |
| `orders` | `status` | B-tree | Filter orders by status (e.g., active shipments). |
| `orders` | `order_number` | B-tree (UNIQUE) | Order lookup by human-readable reference. |

---

### 6.4 Full-Text Search Index

The `books.search_vector` column stores a pre-computed `TSVECTOR` combining the book `title`, `description`, and the associated `authors.name`. A GIN index on this column enables fast full-text search across the catalogue.

| Table | Column | Index Type | Rationale |
|-------|--------|-----------|-----------|
| `books` | `search_vector` | GIN | Enables `@@` operator-based full-text search. Supports the catalogue search bar (title, description, author name). |

The `search_vector` is kept up to date via a PostgreSQL trigger (or application-layer update) whenever `books.title`, `books.description`, or the associated author's `name` changes.

---

### 6.5 JSONB Index

| Table | Column | Index Type | Rationale |
|-------|--------|-----------|-----------|
| `orders` | `delivery_address_snapshot` | GIN | Enables queries against the JSONB address snapshot if needed for reporting or address search in Phase 2. Optional — deferred until query patterns are established. |

---

## 7. Data Integrity Rules

### 7.1 Referential Integrity

All parent-child relationships are enforced with declared foreign key constraints. The `ON DELETE` strategies are chosen to reflect the business rules:

| Strategy | Applied To | Business Rationale |
|----------|-----------|-------------------|
| **CASCADE** | `addresses`, `carts`, `cart_items`, `wishlists`, `wishlist_items`, `reviews`, `user_followed_authors` | These are user-owned records with no independent business value. Deleting a user removes all their dependent data. |
| **RESTRICT** | `orders`, `payments`, `order_items → books`, `books → authors` | Orders and payments are financial records and must never be silently deleted. An author with published books cannot be removed. |
| **SET NULL** | `books → publishers`, `orders → coupons` | Book records remain valid if a publisher is removed. Order records remain valid if a coupon is later deleted. |

---

### 7.2 Order Immutability

Once an order is placed (status transitions from `PLACED`), the following fields are immutable:

- `user_id`, `subtotal`, `tax_amount`, `delivery_charge`, `discount_amount`, `total_amount`, `coupon_id`, `delivery_address_snapshot`, `placed_at`
- All `order_items` columns

Enforcement: the application service layer prohibits updates to these columns after placement. Only the `status` and `estimated_delivery_date` columns may be updated by the system.

---

### 7.3 Price and Financial Accuracy

All monetary columns (`price`, `subtotal`, `tax_amount`, `delivery_charge`, `discount_amount`, `total_amount`, `unit_price`, `line_total`, `payable_amount`, `discount_value`, `min_order_value`) use `NUMERIC(10,2)` to avoid floating-point precision errors inherent in `FLOAT` or `DOUBLE PRECISION` types.

---

### 7.4 Single Active Cart and Wishlist per User

The `UNIQUE` constraints on `carts.user_id` and `wishlists.user_id` guarantee at the database level that no more than one cart and one wishlist record can exist per user, regardless of any application-layer race conditions.

---

### 7.5 Duplicate Prevention in Join Tables and Item Tables

- `book_genres (book_id, genre_id)` composite PK prevents assigning the same genre to a book twice.
- `user_followed_authors (user_id, author_id)` composite PK prevents following the same author twice.
- `cart_items (cart_id, book_id)` UNIQUE constraint prevents the same book appearing as two separate cart rows; re-adding the same book increments `quantity`.
- `wishlist_items (wishlist_id, book_id)` UNIQUE constraint prevents duplicate wishlist entries for the same book.
- `reviews (book_id, user_id)` UNIQUE constraint enforces one review per user per book.

---

### 7.6 Coupon Validity at Application Layer

The database stores the coupon's state (`is_active`, `expires_at`, `min_order_value`). The application service layer validates all three conditions before applying a coupon:
1. `is_active = TRUE`
2. `expires_at > NOW()`
3. Order `subtotal >= min_order_value`

This three-part check is performed inside a database transaction to prevent race conditions where a coupon expires between validation and order creation.

---

### 7.7 Soft Delete for Users and Books

Both `users.is_active` and `books.is_active` implement soft-delete patterns:

- **Users:** Setting `is_active = FALSE` disables login without removing any historical data (orders, reviews). Hard deletion is deferred to a data retention process.
- **Books:** Setting `is_active = FALSE` removes a book from catalogue queries (`WHERE is_active = TRUE`) without removing its reference from existing orders or wishlists.

---

## 8. Design Decisions

### 8.1 `BIGSERIAL` Surrogate Keys vs. UUIDs

`BIGSERIAL` auto-increment integers are used as primary keys rather than UUIDs. The rationale:

- Integers are 8 bytes vs. 16 bytes for UUID, reducing index size and JOIN performance overhead.
- `BIGSERIAL` keys are inherently ordered, which aids `ORDER BY id` queries and B-tree index efficiency.
- The application does not expose raw database IDs in public URLs (the `order_number` field provides the human-readable public identifier for orders). Where URL exposure is needed (book ID, author ID), integer IDs are acceptable.
- If horizontal sharding becomes necessary in a future phase, the migration to UUID or a distributed ID scheme can be made via Flyway.

---

### 8.2 `TIMESTAMPTZ` for All Timestamps

All timestamp columns use `TIMESTAMPTZ` (timestamp with time zone) rather than `TIMESTAMP`. This ensures:

- All stored timestamps are normalised to UTC in the database.
- The application can present them in any local timezone without data conversion issues.
- Delivery date calculations and coupon expiry checks are unambiguous across timezone boundaries.

---

### 8.3 `JSONB` for Delivery Address Snapshot

Rather than creating a separate `order_addresses` table or adding a `snapshot_version` system to the `addresses` table, the delivery address is stored as `JSONB` directly on the `orders` row. The rationale:

- The address snapshot is written once and never updated — JSONB's append-only access pattern is optimal for this use case.
- It avoids a complex temporal data model (address versioning) for a simple historical accuracy requirement.
- JSONB is natively indexed and queryable in PostgreSQL if address-level reporting is needed in Phase 2.
- The snapshot format mirrors the `addresses` table structure, making application-layer serialisation trivial.

---

### 8.4 Price Snapshots on `order_items`

Book `price`, `title`, `cover_image_url`, and `format` are copied to `order_items` at order placement rather than reading them from `books` at display time. The rationale:

- Protects order history display accuracy if a book's price is changed, its title is updated, or the book is deactivated.
- The `book_id` foreign key is retained alongside the snapshots so the "Buy Again" feature can still navigate to the current live book page.
- This is standard practice for eCommerce order line items and avoids complex `AS-OF` temporal queries.

---

### 8.5 `copies_sold` as a Denormalized Counter

Rather than computing the Bestsellers ranking by `COUNT` + `SUM` across `order_items` on every home page load, a `copies_sold` counter is maintained directly on the `books` table. This counter is incremented atomically by the `CheckoutService` inside the order placement transaction. The rationale:

- Avoids an expensive aggregation query across potentially millions of `order_items` rows on the highest-traffic endpoint (home page).
- The counter is always consistent within an ACID transaction boundary.
- A periodic reconciliation job can verify the counter against actual `order_items` counts as a data quality check.

---

### 8.6 Full-Text Search via `TSVECTOR`

A computed `search_vector` column on `books` stores a weighted `TSVECTOR` combining title, description, and author name. A GIN index on this column enables native PostgreSQL full-text search (`@@` operator) without requiring an external search engine. The rationale:

- Meets Phase 1 performance requirements (sub-1-second search on up to 100,000 books) without the operational overhead of Elasticsearch.
- The `tsvector` can weight title matches above description matches using PostgreSQL `setweight()`, improving result relevance.
- Migrating to Elasticsearch in Phase 2 is a non-breaking change at the database level.

---

### 8.7 One Coupon Per Order, No Stacking

The `orders` table has a single nullable `coupon_id` column. This enforces the business rule that only one coupon code may be applied per order. Coupon stacking requires a separate `order_coupons` junction table, which is deferred to Phase 2.

---

### 8.8 `ON DELETE RESTRICT` for Financial Records

Orders, payments, and order items use `RESTRICT` on FK deletions because they constitute financial transaction records. Accidental or cascading deletion of these records would create an unauditable gap in purchase history. Any removal of these records must be an explicit, audited administrative operation.

---

*This document was generated as part of the AI-Assisted Software Development project using IBM Bob.*  
*Document Status: Draft v1.0 — Pending database architect review and sign-off before Flyway migration scripts are authored.*
