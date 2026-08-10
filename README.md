# BookStore — E-Commerce Book Store

A full-stack, production-quality eCommerce platform for discovering, browsing, and purchasing books online. Built as an AI-Assisted Software Engineering Proof of Concept using [IBM Bob](https://www.ibm.com/products/watsonx-code-assistant).

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Folder Structure](#3-folder-structure)
4. [Local Setup](#4-local-setup)
5. [Backend Startup](#5-backend-startup)
6. [Frontend Startup](#6-frontend-startup)
7. [Sample Login Credentials](#7-sample-login-credentials)
8. [API Documentation](#8-api-documentation)
9. [Build Commands](#9-build-commands)
10. [Deployment](#10-deployment)
11. [Environment Variables](#11-environment-variables)

---

## 1. Project Overview

**BookStore** is a single-storefront online bookstore targeting the Indian market. It supports the full customer journey — from unauthenticated browsing and discovery through authenticated checkout and order confirmation.

### Key Features

| Feature | Description |
|---------|-------------|
| **Browse & Discover** | Home page with Recommended, Bestsellers, and New Launches sections |
| **Catalogue** | Full-text search, genre filter sidebar, language/format/price filters, sorting, pagination |
| **Book Detail** | Cover, description, author bio, star ratings, reviews, related reads, Add to Cart / Wishlist |
| **Authentication** | Login (email/phone), Register, Forgot Password, Continue as Guest |
| **Shopping Cart** | Add items, adjust quantities, remove items, cart badge count |
| **Checkout** | Delivery address form, saved address autofill, coupon validation, grand total breakdown |
| **Payment** | Credit Card, Debit Card, UPI, Wallet — Phase 1 mock gateway |
| **Order Confirmation** | Success screen with purchased books summary |
| **My Orders** | Full purchase history, Buy Again |
| **My Wishlist** | Add/remove books, move to cart |
| **My Writers** | Follow/unfollow authors, see their recent books |
| **Author Profile** | Bio, full book catalogue, follow button |
| **My Account** | Profile update, saved address CRUD, set default |

---

## 2. Technology Stack

### Backend

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.3 |
| Security | Spring Security 6 + JJWT 0.12 |
| Data Access | Spring Data JPA + Hibernate |
| Database | PostgreSQL 15+ |
| Mapping | MapStruct 1.5 |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Apache Maven |

### Frontend

| Layer | Technology |
|-------|-----------|
| Framework | React 18 |
| Build Tool | Vite 5 |
| UI Library | Material UI v5 (dark theme) |
| Routing | React Router v6 |
| Server State | TanStack React Query v5 |
| HTTP Client | Axios |
| Styling | MUI `sx` prop + global CSS |

---

## 3. Folder Structure

```
book-store/
├── backend/                    # Spring Boot REST API
│   ├── src/main/java/com/bookstore/
│   │   ├── controller/         # REST controllers
│   │   ├── service/            # Business logic
│   │   ├── repository/         # Spring Data JPA repositories
│   │   ├── domain/             # JPA entities + enums
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── mapper/             # MapStruct mappers
│   │   ├── security/           # JWT + Spring Security config
│   │   ├── config/             # CORS, Swagger config
│   │   └── common/             # Shared utilities, constants, response wrapper
│   └── src/main/resources/
│       ├── application.yml         # Base configuration
│       ├── application-dev.yml     # Dev overrides (create-drop, debug logs)
│       ├── application-prod.yml    # Prod overrides (validate, no Swagger)
│       └── data.sql                # Demo seed data (loaded on dev startup)
│
├── frontend/                   # React + Vite SPA
│   └── src/
│       ├── App.jsx             # Root router
│       ├── main.jsx            # Bootstrap (providers)
│       ├── theme/              # MUI dark theme
│       ├── common/
│       │   ├── api/            # Axios client + feature API modules
│       │   ├── components/     # Shared UI components
│       │   ├── constants/      # Route paths, React Query keys
│       │   ├── context/        # AuthContext, CartContext
│       │   ├── hooks/          # useAuth, useCart, useDebounce
│       │   ├── layout/         # MainLayout (Navbar + Footer)
│       │   └── utils/          # formatCurrency, formatDate, validators
│       └── features/
│           ├── auth/           # LoginModal, RegisterForm, ForgotPassword
│           ├── home/           # HomePage, BookRow
│           ├── catalogue/      # CataloguePage, BookGrid
│           ├── bookDetail/     # BookDetailPage, Reviews, RelatedReads
│           ├── author/         # AuthorProfilePage
│           ├── cart/           # CheckoutPage
│           ├── payment/        # PaymentModal, OrderConfirmationPage
│           ├── orders/         # MyOrdersPage
│           ├── wishlist/       # WishlistPage
│           ├── myWriters/      # MyWritersPage
│           └── profile/        # ProfilePage, AddressDialog
│
├── api/
│   ├── api-design.md           # REST API design document
│   └── openapi.yaml            # OpenAPI 3 specification
├── project/
│   ├── requirements.md         # SRS
│   └── architecture.md         # Solution architecture
└── screenshots/                # UI reference screenshots
```

---

## 4. Local Setup

### Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java | 17+ | OpenJDK or Oracle JDK |
| Maven | 3.9+ | Or use `./mvnw` wrapper |
| Node.js | 18+ | v22 recommended |
| npm | 9+ | Ships with Node |
| PostgreSQL | 15+ | Local install or Docker |

### 4.1 PostgreSQL Setup

```sql
-- Run as postgres superuser
CREATE DATABASE bookstore_dev;
CREATE USER bookstore WITH PASSWORD 'bookstore';
GRANT ALL PRIVILEGES ON DATABASE bookstore_dev TO bookstore;
GRANT ALL ON SCHEMA public TO bookstore;
```

> **Tip:** Using Docker:  
> `docker run -d --name pg -e POSTGRES_DB=bookstore_dev -e POSTGRES_USER=bookstore -e POSTGRES_PASSWORD=bookstore -p 5432:5432 postgres:15`

### 4.2 Clone the repository

```bash
git clone https://github.com/your-org/book-store.git
cd book-store
```

---

## 5. Backend Startup

```bash
cd backend

# Run with dev profile (creates schema + loads data.sql)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or with explicit environment variables
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

The backend starts on **http://localhost:8080**

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Swagger UI (dev only) |
| `http://localhost:8080/api-docs` | OpenAPI JSON |
| `http://localhost:8080/actuator/health` | Health check |

### First-time Startup

On the **first run** with `ddl-auto: create-drop` (dev profile):

1. Hibernate creates all tables from JPA entity definitions.
2. `data.sql` is loaded automatically, inserting genres, publishers, authors, 15 books, 2 users, sample orders, wishlist, cart, and coupons.
3. The application is ready for immediate demonstration.

> **Note:** `create-drop` drops and recreates the schema on every restart. All seed data is reloaded fresh each time. Switch to `ddl-auto: update` to preserve data across restarts.

---

## 6. Frontend Startup

```bash
cd frontend

# Install dependencies (first time)
npm install

# Start development server
npm run dev
```

The frontend starts on **http://localhost:3000**

The dev server proxies `/api` requests to `http://localhost:8080` (configured in `vite.config.js`).

---

## 7. Sample Login Credentials

| Role | Email | Password |
|------|-------|----------|
| **Admin** | `admin@bookstore.com` | `Admin@123!` |
| **Customer** | `customer@bookstore.com` | `Customer@123!` |

> **Password requirements:** minimum 8 characters, at least one uppercase letter, one digit, and one special character.

### Demo Account Features

The **Customer** account (`customer@bookstore.com`) is pre-loaded with:
- 2 books in the wishlist (The Midnight Hour, The Final Frontier)
- 2 books in the cart (The Joy of Minimalism, The Path to Success)
- 2 past orders (1 DELIVERED, 1 CONFIRMED)
- 3 book reviews
- 2 followed authors (Daniel Reed, Laura Mitchell)
- 1 saved delivery address

---

## 8. API Documentation

Interactive Swagger UI is available at `http://localhost:8080/swagger-ui.html` when running with the `dev` profile.

The full REST API contract is documented in:
- [`api/api-design.md`](api/api-design.md) — Human-readable design document
- [`api/openapi.yaml`](api/openapi.yaml) — Machine-readable OpenAPI 3 specification

### API Base URL

```
http://localhost:8080/api/v1
```

### Authentication

All protected endpoints require a `Bearer` JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Tokens are obtained from `POST /api/v1/auth/login` or `POST /api/v1/auth/register`.

---

## 9. Build Commands

### Backend

```bash
cd backend

# Compile and run tests
./mvnw clean test

# Build production JAR
./mvnw clean package -DskipTests

# Run the JAR directly
java -jar target/book-store-backend-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --DB_URL=jdbc:postgresql://... \
  --DB_USERNAME=... \
  --DB_PASSWORD=... \
  --JWT_SECRET=...
```

### Frontend

```bash
cd frontend

# Production build
npm run build

# Preview production build locally
npm run preview

# Lint
npm run lint
```

---

## 10. Deployment

### 10.1 Backend — Render (Web Service)

1. Create a new **Web Service** on [Render](https://render.com)
2. Connect your GitHub repository
3. Set the following:

| Setting | Value |
|---------|-------|
| **Runtime** | Java |
| **Root Directory** | `backend` |
| **Build Command** | `./mvnw clean package -DskipTests` |
| **Start Command** | `java -jar target/book-store-backend-1.0.0-SNAPSHOT.jar` |

4. Add all environment variables listed in [Section 11](#11-environment-variables).

### 10.2 Database — Neon PostgreSQL

1. Create a free project on [Neon](https://neon.tech)
2. Create a database named `bookstore`
3. Copy the connection string and set as `DB_URL`
4. Set `SPRING_PROFILES_ACTIVE=prod`

> For production, run the schema manually once (export from dev via `pg_dump --schema-only`) and set `spring.jpa.hibernate.ddl-auto=validate`.

### 10.3 Frontend — Vercel

1. Import your repository on [Vercel](https://vercel.com)
2. Set the following:

| Setting | Value |
|---------|-------|
| **Framework Preset** | Vite |
| **Root Directory** | `frontend` |
| **Build Command** | `npm run build` |
| **Output Directory** | `dist` |

3. Add environment variable `VITE_API_BASE_URL` pointing to your Render backend URL.
4. Add a `vercel.json` at `frontend/vercel.json` to handle SPA routing:

```json
{
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```

---

## 11. Environment Variables

### Backend (required for production)

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `DB_URL` | JDBC connection URL | `jdbc:postgresql://host:5432/bookstore` |
| `DB_USERNAME` | Database username | `bookstore` |
| `DB_PASSWORD` | Database password | `s3cret` |
| `JWT_SECRET` | JWT signing key (min 256-bit / 32 chars) | `your-256-bit-secret-here` |
| `JWT_EXPIRATION_MS` | Token TTL in milliseconds | `86400000` (24h) |
| `SERVER_PORT` | HTTP port | `8080` |
| `DELIVERY_OFFSET_DAYS` | Estimated delivery buffer (days) | `7` |
| `TAX_RATE` | GST rate as decimal | `0.12` |

Copy `backend/.env.example` and fill in values.

### Frontend (required for production)

| Variable | Description | Example |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API base URL | `https://book-store.onrender.com/api/v1` |
| `VITE_APP_NAME` | Application name | `BookStore` |

Copy `frontend/.env.example` and fill in values.

---

## Screenshots

| Screen | File |
|--------|------|
| Home Page | `screenshots/01-homepage-overview.png` |
| Login / Registration | `screenshots/02-login-registration.png` |
| Book Catalogue | `screenshots/03-book-catalog.png` |
| Book Detail & Cart | `screenshots/04-book-details-cart.png` |
| Checkout & Address | `screenshots/05-checkout-address.png` |
| Payment Gateway | `screenshots/06-payment-gateway.png` |
| Order Confirmation | `screenshots/07-order-confirmation.png` |

---

## Development Notes

- **CORS:** The backend is configured to allow requests from `http://localhost:3000` (dev) and the configured production origin via the `CorsConfig` bean.
- **JWT Token:** 24-hour expiry by default. The frontend stores the token in `localStorage` and attaches it to every request via an Axios request interceptor.
- **Payment:** Phase 1 uses a mock payment gateway — all valid requests return `SUCCESS`. No real financial transactions are made.
- **Recommendation Engine:** Phase 1 uses simple rule-based recommendations (books in the same genres as the user's purchase history).

---

*Built with [IBM Bob](https://www.ibm.com/products/watsonx-code-assistant) — AI-Assisted Software Engineering.*
