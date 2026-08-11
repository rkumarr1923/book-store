-- =============================================================================
-- BookStore — Production Schema Initialisation Script
-- =============================================================================
-- Purpose  : Create all tables, constraints, and indexes for the first
--            production deployment on a fresh Neon PostgreSQL database.
--
-- IMPORTANT USAGE NOTES
-- ─────────────────────
-- 1. Run this script ONCE against an empty production database before the
--    first application deployment.
-- 2. Do NOT run this script against an existing database that already has
--    data — it uses CREATE TABLE IF NOT EXISTS guards but repeated execution
--    on a schema that already differs from this file may cause errors.
-- 3. This script contains NO seed data, NO passwords, NO user records,
--    NO demo books, NO coupons, and NO BCrypt hashes.
-- 4. data.sql is intentionally disabled in production
--    (spring.sql.init.mode=never in application-prod.yml).
-- 5. The application uses spring.jpa.hibernate.ddl-auto=validate in
--    production — Hibernate validates the schema against entity metadata
--    but never modifies it.  This script IS that schema.
-- 6. After running this script, deploy the application JAR/container with
--    SPRING_PROFILES_ACTIVE=prod and the required environment variables.
--
-- PostgreSQL version : 18 (Neon — compatible with PostgreSQL 14+)
-- Generated from     : JPA entity inspection (com.bookstore.domain.*)
-- =============================================================================

-- Wrap everything in a transaction so the schema is applied atomically.
BEGIN;

-- =============================================================================
-- SECTION 1 — INDEPENDENT TABLES (no foreign keys to other app tables)
-- =============================================================================

-- ─── genres ──────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Genre (extends BaseCreatedEntity)
-- id         : BIGSERIAL  — GenerationType.IDENTITY
-- created_at : TIMESTAMPTZ — Spring Data @CreatedDate (Instant)

CREATE TABLE IF NOT EXISTS genres (
    id         BIGSERIAL     NOT NULL,
    name       VARCHAR(100)  NOT NULL,
    slug       VARCHAR(100)  NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL,

    CONSTRAINT pk_genres         PRIMARY KEY (id),
    CONSTRAINT uk_genres_name    UNIQUE      (name),
    CONSTRAINT uk_genres_slug    UNIQUE      (slug)
);

-- ─── publishers ──────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Publisher (extends BaseCreatedEntity)

CREATE TABLE IF NOT EXISTS publishers (
    id         BIGSERIAL    NOT NULL,
    name       VARCHAR(255) NOT NULL,
    website    VARCHAR(500),
    created_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_publishers      PRIMARY KEY (id),
    CONSTRAINT uk_publishers_name UNIQUE      (name)
);

-- ─── authors ─────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Author (extends BaseCreatedEntity)

CREATE TABLE IF NOT EXISTS authors (
    id                BIGSERIAL    NOT NULL,
    name              VARCHAR(255) NOT NULL,
    biography         TEXT,
    profile_image_url VARCHAR(500),
    created_at        TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_authors      PRIMARY KEY (id),
    CONSTRAINT uk_authors_name UNIQUE      (name)
);

-- ─── users ───────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.User (extends BaseEntity)
-- created_at + updated_at — Spring Data auditing (Instant → TIMESTAMPTZ)
-- role       : UserRole enum stored as VARCHAR string
-- is_active  : boolean (Hibernate maps Java boolean → BOOLEAN)

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL    NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_users              PRIMARY KEY (id),
    CONSTRAINT uk_users_email        UNIQUE      (email),
    CONSTRAINT uk_users_phone_number UNIQUE      (phone_number),
    CONSTRAINT ck_users_role         CHECK       (role IN ('CUSTOMER', 'ADMIN'))
);

-- ─── coupons ─────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Coupon (extends BaseCreatedEntity)
-- discount_type     : DiscountType enum → VARCHAR
-- max_usage_per_user: INTEGER NOT NULL DEFAULT 0
--   0 = unlimited, >0 = per-user cap counted against CONFIRMED/SHIPPED/DELIVERED orders

CREATE TABLE IF NOT EXISTS coupons (
    id                  BIGSERIAL      NOT NULL,
    code                VARCHAR(50)    NOT NULL,
    discount_type       VARCHAR(20)    NOT NULL,
    discount_value      NUMERIC(8, 2)  NOT NULL,
    min_order_value     NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    expires_at          TIMESTAMPTZ    NOT NULL,
    is_active           BOOLEAN        NOT NULL DEFAULT TRUE,
    max_usage_per_user  INTEGER        NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ    NOT NULL,

    CONSTRAINT pk_coupons               PRIMARY KEY (id),
    CONSTRAINT uk_coupons_code          UNIQUE      (code),
    CONSTRAINT ck_coupons_discount_type CHECK       (discount_type IN ('FIXED', 'PERCENTAGE')),
    CONSTRAINT ck_coupons_discount_val  CHECK       (discount_value > 0),
    CONSTRAINT ck_coupons_min_order     CHECK       (min_order_value >= 0),
    CONSTRAINT ck_coupons_max_usage     CHECK       (max_usage_per_user >= 0)
);

-- =============================================================================
-- SECTION 2 — BOOKS (depends on authors, publishers)
-- =============================================================================

-- ─── books ───────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Book (extends BaseEntity)
-- format        : BookFormat enum → VARCHAR (PAPERBACK | HARDCOVER | EBOOK)
-- copies_sold   : INTEGER NOT NULL DEFAULT 0
-- search_vector : TEXT — Hibernate maps as TEXT (insertable=false, updatable=false)
--                 NOTE: The actual column type in a full FTS setup would be tsvector.
--                 It is created here as TEXT to exactly match the JPA mapping
--                 (columnDefinition="TEXT"). A separate DB migration (Phase 2) can
--                 ALTER this column to tsvector and install the trigger.
-- price         : NUMERIC(10,2) — precision=10, scale=2 per @Digits(integer=8, fraction=2)

CREATE TABLE IF NOT EXISTS books (
    id               BIGSERIAL      NOT NULL,
    title            VARCHAR(500)   NOT NULL,
    description      TEXT,
    cover_image_url  VARCHAR(500),
    isbn             VARCHAR(20),
    language         VARCHAR(50)    NOT NULL DEFAULT 'English',
    format           VARCHAR(20)    NOT NULL,
    price            NUMERIC(10, 2) NOT NULL,
    copies_sold      INTEGER        NOT NULL DEFAULT 0,
    published_date   DATE,
    is_active        BOOLEAN        NOT NULL DEFAULT TRUE,
    search_vector    TEXT,
    created_at       TIMESTAMPTZ    NOT NULL,
    updated_at       TIMESTAMPTZ    NOT NULL,
    author_id        BIGINT         NOT NULL,
    publisher_id     BIGINT,

    CONSTRAINT pk_books         PRIMARY KEY (id),
    CONSTRAINT uk_books_isbn    UNIQUE      (isbn),
    CONSTRAINT ck_books_format  CHECK       (format IN ('PAPERBACK', 'HARDCOVER', 'EBOOK')),
    CONSTRAINT ck_books_price   CHECK       (price > 0),
    CONSTRAINT ck_books_copies  CHECK       (copies_sold >= 0),
    CONSTRAINT fk_books_author
        FOREIGN KEY (author_id)    REFERENCES authors    (id),
    CONSTRAINT fk_books_publisher
        FOREIGN KEY (publisher_id) REFERENCES publishers (id)
);

-- ─── book_genres (join table) ─────────────────────────────────────────────────
-- Maps to: Book.genres @ManyToMany join table
-- No surrogate key — PK is the (book_id, genre_id) pair.

CREATE TABLE IF NOT EXISTS book_genres (
    book_id  BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,

    CONSTRAINT pk_book_genres PRIMARY KEY (book_id, genre_id),
    CONSTRAINT fk_book_genres_book
        FOREIGN KEY (book_id)  REFERENCES books  (id) ON DELETE CASCADE,
    CONSTRAINT fk_book_genres_genre
        FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE CASCADE
);

-- =============================================================================
-- SECTION 3 — USER-DEPENDENT TABLES (addresses, cart, wishlist, orders)
-- =============================================================================

-- ─── addresses ───────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Address (extends BaseCreatedEntity)
-- Cascade-deleted when user is deleted (CascadeType.ALL + orphanRemoval in User)

CREATE TABLE IF NOT EXISTS addresses (
    id            BIGSERIAL    NOT NULL,
    user_id       BIGINT       NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city          VARCHAR(100) NOT NULL,
    state         VARCHAR(100) NOT NULL,
    country       VARCHAR(100) NOT NULL DEFAULT 'India',
    pin_code      VARCHAR(10)  NOT NULL,
    phone_number  VARCHAR(20)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    is_default    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_addresses       PRIMARY KEY (id),
    CONSTRAINT fk_addresses_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ─── carts ───────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Cart (extends BaseEntity → has updated_at too)
-- One cart per user enforced by uk_carts_user_id.

CREATE TABLE IF NOT EXISTS carts (
    id         BIGSERIAL   NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_carts         PRIMARY KEY (id),
    CONSTRAINT uk_carts_user_id UNIQUE      (user_id),
    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ─── cart_items ───────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.CartItem (extends BaseCreatedEntity)
-- uk_cart_items_cart_book prevents duplicate book in same cart.
-- quantity DEFAULT 1 matches entity @Builder.Default

CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGSERIAL NOT NULL,
    cart_id    BIGINT    NOT NULL,
    book_id    BIGINT    NOT NULL,
    quantity   INTEGER   NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_cart_items           PRIMARY KEY (id),
    CONSTRAINT uk_cart_items_cart_book UNIQUE      (cart_id, book_id),
    CONSTRAINT ck_cart_items_qty       CHECK       (quantity >= 1),
    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_book
        FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

-- ─── wishlists ────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Wishlist (extends BaseCreatedEntity)
-- One wishlist per user enforced by uk_wishlists_user_id.

CREATE TABLE IF NOT EXISTS wishlists (
    id         BIGSERIAL   NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_wishlists         PRIMARY KEY (id),
    CONSTRAINT uk_wishlists_user_id UNIQUE      (user_id),
    CONSTRAINT fk_wishlists_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ─── wishlist_items ───────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.WishlistItem (extends BaseCreatedEntity)
-- uk_wishlist_items_wishlist_book prevents same book appearing twice.

CREATE TABLE IF NOT EXISTS wishlist_items (
    id          BIGSERIAL   NOT NULL,
    wishlist_id BIGINT      NOT NULL,
    book_id     BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_wishlist_items                PRIMARY KEY (id),
    CONSTRAINT uk_wishlist_items_wishlist_book  UNIQUE      (wishlist_id, book_id),
    CONSTRAINT fk_wishlist_items_wishlist
        FOREIGN KEY (wishlist_id) REFERENCES wishlists (id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_items_book
        FOREIGN KEY (book_id)     REFERENCES books     (id) ON DELETE CASCADE
);

-- ─── orders ───────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Order (extends BaseEntity)
-- status                   : OrderStatus enum → VARCHAR
-- delivery_address_snapshot: JSONB — immutable address capture at order time
-- placed_at                : TIMESTAMPTZ (updatable=false — set at construction)
-- estimated_delivery_date  : DATE (LocalDate → DATE)
-- coupon_id                : nullable FK — NULL when no coupon applied

CREATE TABLE IF NOT EXISTS orders (
    id                         BIGSERIAL       NOT NULL,
    order_number               VARCHAR(30)     NOT NULL,
    user_id                    BIGINT          NOT NULL,
    status                     VARCHAR(20)     NOT NULL DEFAULT 'PLACED',
    subtotal                   NUMERIC(10, 2)  NOT NULL,
    tax_amount                 NUMERIC(10, 2)  NOT NULL DEFAULT 0.00,
    delivery_charge            NUMERIC(10, 2)  NOT NULL DEFAULT 0.00,
    discount_amount            NUMERIC(10, 2)  NOT NULL DEFAULT 0.00,
    total_amount               NUMERIC(10, 2)  NOT NULL,
    coupon_id                  BIGINT,
    delivery_address_snapshot  JSONB           NOT NULL,
    placed_at                  TIMESTAMPTZ     NOT NULL,
    estimated_delivery_date    DATE            NOT NULL,
    created_at                 TIMESTAMPTZ     NOT NULL,
    updated_at                 TIMESTAMPTZ     NOT NULL,

    CONSTRAINT pk_orders              PRIMARY KEY (id),
    CONSTRAINT uk_orders_order_number UNIQUE      (order_number),
    CONSTRAINT ck_orders_status       CHECK       (status IN ('PLACED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')),
    CONSTRAINT ck_orders_subtotal     CHECK       (subtotal     >= 0),
    CONSTRAINT ck_orders_tax          CHECK       (tax_amount   >= 0),
    CONSTRAINT ck_orders_delivery     CHECK       (delivery_charge  >= 0),
    CONSTRAINT ck_orders_discount     CHECK       (discount_amount  >= 0),
    CONSTRAINT ck_orders_total        CHECK       (total_amount >= 0),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)    REFERENCES users    (id),
    CONSTRAINT fk_orders_coupon
        FOREIGN KEY (coupon_id)  REFERENCES coupons  (id)
);

-- ─── order_items ─────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.OrderItem (extends BaseCreatedEntity)
-- title / cover_image_url / format / unit_price are snapshot columns —
--   they preserve the book's state at order time even if the catalogue changes.
-- book_id FK uses default (RESTRICT) — deleting a book with order history is blocked.

CREATE TABLE IF NOT EXISTS order_items (
    id              BIGSERIAL      NOT NULL,
    order_id        BIGINT         NOT NULL,
    book_id         BIGINT         NOT NULL,
    title           VARCHAR(500)   NOT NULL,
    cover_image_url VARCHAR(500),
    format          VARCHAR(20)    NOT NULL,
    unit_price      NUMERIC(10, 2) NOT NULL,
    quantity        INTEGER        NOT NULL,
    line_total      NUMERIC(10, 2) NOT NULL,
    created_at      TIMESTAMPTZ    NOT NULL,

    CONSTRAINT pk_order_items        PRIMARY KEY (id),
    CONSTRAINT ck_order_items_format CHECK       (format IN ('PAPERBACK', 'HARDCOVER', 'EBOOK')),
    CONSTRAINT ck_order_items_price  CHECK       (unit_price > 0),
    CONSTRAINT ck_order_items_qty    CHECK       (quantity   >= 1),
    CONSTRAINT ck_order_items_total  CHECK       (line_total > 0),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_book
        FOREIGN KEY (book_id)  REFERENCES books  (id)
        -- Default behaviour (RESTRICT): prevents accidental book deletion with order history
);

-- ─── payments ─────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Payment (extends BaseCreatedEntity)
-- One payment per order enforced by uk_payments_order_id.
-- payment_method : PaymentMethod enum → VARCHAR
-- status         : PaymentStatus enum → VARCHAR
-- paid_at        : nullable — NULL until payment completes

CREATE TABLE IF NOT EXISTS payments (
    id                    BIGSERIAL      NOT NULL,
    order_id              BIGINT         NOT NULL,
    payment_method        VARCHAR(20)    NOT NULL,
    payable_amount        NUMERIC(10, 2) NOT NULL,
    status                VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    transaction_reference VARCHAR(255),
    paid_at               TIMESTAMPTZ,
    created_at            TIMESTAMPTZ    NOT NULL,

    CONSTRAINT pk_payments              PRIMARY KEY (id),
    CONSTRAINT uk_payments_order_id     UNIQUE      (order_id),
    CONSTRAINT ck_payments_method       CHECK       (payment_method IN ('CREDIT_CARD','DEBIT_CARD','UPI','WALLET')),
    CONSTRAINT ck_payments_status       CHECK       (status         IN ('PENDING','SUCCESS','FAILED')),
    CONSTRAINT ck_payments_amount       CHECK       (payable_amount > 0),
    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

-- =============================================================================
-- SECTION 4 — REVIEWS
-- =============================================================================

-- ─── reviews ─────────────────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.Review (extends BaseCreatedEntity)
-- uk_reviews_book_user enforces one review per user per book.
-- rating : 1–5 enforced by check constraint

CREATE TABLE IF NOT EXISTS reviews (
    id          BIGSERIAL    NOT NULL,
    book_id     BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    rating      INTEGER      NOT NULL,
    review_text VARCHAR(100),
    created_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_reviews          PRIMARY KEY (id),
    CONSTRAINT uk_reviews_book_user UNIQUE     (book_id, user_id),
    CONSTRAINT ck_reviews_rating   CHECK       (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_reviews_book
        FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- =============================================================================
-- SECTION 5 — USER–AUTHOR FOLLOW RELATIONSHIP
-- =============================================================================

-- ─── user_followed_authors ────────────────────────────────────────────────────
-- Maps to: com.bookstore.domain.UserFollowedAuthor
-- Composite PK (user_id, author_id) — matches @EmbeddedId UserFollowedAuthorId.
-- followed_at : TIMESTAMPTZ (updatable=false — set by @CreatedDate)

CREATE TABLE IF NOT EXISTS user_followed_authors (
    user_id     BIGINT      NOT NULL,
    author_id   BIGINT      NOT NULL,
    followed_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_user_followed_authors PRIMARY KEY (user_id, author_id),
    CONSTRAINT fk_followed_authors_user
        FOREIGN KEY (user_id)   REFERENCES users   (id) ON DELETE CASCADE,
    CONSTRAINT fk_followed_authors_author
        FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE CASCADE
);

-- =============================================================================
-- SECTION 6 — PERFORMANCE INDEXES
-- =============================================================================
-- These indexes support the most common read patterns observed in the repositories.
-- Unique constraint indexes are automatically created by the UNIQUE declarations above.

-- books: catalogue browsing and filtering
CREATE INDEX IF NOT EXISTS idx_books_author_id    ON books (author_id);
CREATE INDEX IF NOT EXISTS idx_books_publisher_id ON books (publisher_id);
CREATE INDEX IF NOT EXISTS idx_books_language     ON books (language);
CREATE INDEX IF NOT EXISTS idx_books_format       ON books (format);
CREATE INDEX IF NOT EXISTS idx_books_is_active    ON books (is_active);
CREATE INDEX IF NOT EXISTS idx_books_copies_sold  ON books (copies_sold DESC);
CREATE INDEX IF NOT EXISTS idx_books_published    ON books (published_date DESC);
CREATE INDEX IF NOT EXISTS idx_books_created_at   ON books (created_at  DESC);

-- book_genres: join traversal
CREATE INDEX IF NOT EXISTS idx_book_genres_genre_id ON book_genres (genre_id);

-- addresses: user address list
CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON addresses (user_id);

-- orders: order history pagination (most queried pattern)
CREATE INDEX IF NOT EXISTS idx_orders_user_id   ON orders (user_id);
CREATE INDEX IF NOT EXISTS idx_orders_placed_at ON orders (placed_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_status    ON orders (status);
-- Composite: user history list filtered by status
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders (user_id, status, placed_at DESC);

-- order_items: items per order
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_book_id  ON order_items (book_id);

-- reviews: book rating aggregation, user review lookup
CREATE INDEX IF NOT EXISTS idx_reviews_book_id ON reviews (book_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews (user_id);

-- wishlist_items: wishlist content lookup
CREATE INDEX IF NOT EXISTS idx_wishlist_items_wishlist_id ON wishlist_items (wishlist_id);

-- cart_items: cart content lookup
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items (cart_id);

-- user_followed_authors: "My Writers" — ordered by recency
CREATE INDEX IF NOT EXISTS idx_followed_authors_user_id     ON user_followed_authors (user_id, followed_at DESC);
CREATE INDEX IF NOT EXISTS idx_followed_authors_author_id   ON user_followed_authors (author_id);

-- coupons: checkout validation (active + non-expired lookup by code)
CREATE INDEX IF NOT EXISTS idx_coupons_code      ON coupons (code);
CREATE INDEX IF NOT EXISTS idx_coupons_is_active ON coupons (is_active, expires_at);

COMMIT;

-- =============================================================================
-- POST-SCRIPT NOTES
-- =============================================================================
--
-- SEQUENCE AUTO-INCREMENT
-- BIGSERIAL columns automatically create sequences named <table>_id_seq.
-- Neon/PostgreSQL manages these automatically — no manual CREATE SEQUENCE needed.
--
-- FULL-TEXT SEARCH (Phase 2)
-- books.search_vector is currently TEXT (matching the JPA mapping).
-- When Phase 2 FTS is implemented:
--   1. ALTER TABLE books ALTER COLUMN search_vector TYPE tsvector
--      USING search_vector::tsvector;
--   2. CREATE INDEX idx_books_fts ON books USING gin(search_vector);
--   3. Install the trigger that populates search_vector on INSERT/UPDATE.
-- This can be done online without downtime on Neon.
--
-- SCHEMA VALIDATION
-- Run with ddl-auto=validate at startup. Hibernate will verify every column
-- name, type, and nullable flag matches the JPA entity metadata.
-- If validation fails, the error message will identify the exact mismatch.
--
-- TABLE COUNT  : 17
-- FOREIGN KEYS : 20
-- =============================================================================
