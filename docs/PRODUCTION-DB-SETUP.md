# Production Database Setup

This document explains how to initialise the BookStore production database on Neon PostgreSQL before the first deployment.

---

## Overview

The application is configured with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate   # validates schema — never modifies it
  sql:
    init:
      mode: never          # data.sql is NOT executed in production
```

Hibernate does **not** create or alter tables in production. You must run the schema script **once** against the empty Neon database before the first application deployment.

---

## Schema script

**File:** [`docs/production-schema.sql`](./production-schema.sql)

### What it contains

| Category | Count |
|---|---|
| Tables | 16 |
| Foreign keys | 21 |
| Unique constraints | 14 |
| Check constraints | 17 |
| Performance indexes | 24 |

### Tables created

| Table | Description |
|---|---|
| `genres` | Book genre / category lookup |
| `publishers` | Publisher records |
| `authors` | Book author records |
| `users` | Registered users (customers and admins) |
| `coupons` | Discount coupon definitions |
| `books` | Book catalogue (product table) |
| `book_genres` | Many-to-many join: books ↔ genres |
| `addresses` | User saved delivery addresses |
| `carts` | Per-user shopping cart (one per user) |
| `cart_items` | Individual items in a cart |
| `wishlists` | Per-user wishlist (one per user) |
| `wishlist_items` | Individual items in a wishlist |
| `orders` | Confirmed customer orders |
| `order_items` | Line items within an order (snapshot) |
| `payments` | Payment transaction records (one per order) |
| `reviews` | Customer book reviews |
| `user_followed_authors` | Many-to-many: users follow authors |

### What it does NOT contain

- No user accounts, passwords, or BCrypt hashes
- No demo / seed data (books, authors, genres, publishers, coupons)
- No Neon credentials or any environment-specific values
- No `DROP TABLE` statements

---

## Running the script

### Prerequisites

- An empty Neon production database (`neondb`, branch `production`)
- `psql` installed locally, OR use the Neon SQL editor in the web console

### Option A — psql (recommended for production)

```bash
psql "postgresql://<username>:<password>@<pooler-host>/neondb?sslmode=require&channel_binding=require" \
  -f docs/production-schema.sql
```

Replace the connection string with your actual Neon pooled URL (from Neon Console → Branches → production → Connection string).

### Option B — Neon SQL editor

1. Open [console.neon.tech](https://console.neon.tech)
2. Select the **production** branch
3. Open **SQL editor**
4. Paste the contents of `docs/production-schema.sql`
5. Click **Run**

---

## After running the script

1. Verify the expected tables exist:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

Expected output (16 tables): `addresses`, `authors`, `book_genres`, `books`, `cart_items`, `carts`, `coupons`, `genres`, `order_items`, `orders`, `payments`, `publishers`, `reviews`, `user_followed_authors`, `users`, `wishlist_items`, `wishlists`.

2. Deploy the application with `SPRING_PROFILES_ACTIVE=prod` and the required environment variables.

3. On first startup, Hibernate will run `ddl-auto=validate` — if the schema matches the entities, the application will start cleanly. Any mismatch will produce a descriptive error.

---

## Seeding initial data

`data.sql` is **not** executed in production (`spring.sql.init.mode=never`).

The first production user (admin account) must be created via one of:

- The `POST /api/v1/auth/register` endpoint (creates a CUSTOMER by default)
- A one-time manual SQL `INSERT` with a BCrypt-hashed password (generate offline using `htpasswd -bnBC 12 "" <password> | tr -d ':\n'` or equivalent)

Catalogue data (books, authors, genres, publishers, coupons) can be added through the admin API once an admin user exists.

---

## Important constraints

| Rule | Detail |
|---|---|
| Run once only | This script is idempotent (`IF NOT EXISTS`) but should not be re-run on a database that already contains data without careful review |
| No rollback script provided | Schema rollback requires manual `DROP TABLE` statements in reverse dependency order |
| `search_vector` column | Currently `TEXT` matching the JPA mapping. Upgrading to `tsvector` + GIN index requires a separate Phase 2 migration script |
| `max_usage_per_user` on coupons | `0` means unlimited. Values > 0 enforce per-user usage caps counted against `CONFIRMED`, `SHIPPED`, and `DELIVERED` orders only |
| `order_items.book_id` FK | Uses default `RESTRICT` — you cannot delete a book that has associated order history |
| `orders.delivery_address_snapshot` | Stored as `JSONB` — preserves the delivery address at order time, independent of future address changes |

---

## Schema change process (post-first-deployment)

After the first deployment, schema changes must be handled as manual migrations:

1. Write a SQL migration script (`docs/migrations/YYYYMMDD_description.sql`)
2. Apply it to the Neon production branch via psql or Neon SQL editor
3. Update `docs/production-schema.sql` to reflect the new baseline
4. Deploy the updated application code

Consider adopting Flyway or Liquibase for automated migration management in a future sprint.
