# BookStore — Code Standards & Best Practices

> **Status:** Pre-deployment review snapshot — August 2026  
> **Scope:** Covers the full-stack BookStore portfolio application (Spring Boot 3.3 backend + React 18 / Vite 5 frontend)

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Implemented & Verified](#2-implemented--verified)
3. [Partially Implemented](#3-partially-implemented)
4. [Not Applicable / Out of Scope](#4-not-applicable--out-of-scope)
5. [Future Recommendations](#5-future-recommendations)
6. [Demo Payment Limitation](#6-demo-payment-limitation)
7. [Deployment Checklist](#7-deployment-checklist)

---

## 1. Project Overview

BookStore is a full-stack e-commerce portfolio application demonstrating a real-world shopping experience for books.

| Layer | Technology |
|---|---|
| Frontend | React 18, Vite 5, Material UI v5, React Router v6, TanStack Query |
| Backend | Spring Boot 3.3, Spring Security 6, Spring Data JPA, PostgreSQL |
| Auth | JWT (HMAC-SHA256), stateless sessions |
| Email | JavaMailSender → Mailtrap sandbox (dev) / configurable SMTP (prod) |
| Database | PostgreSQL 15 |

---

## 2. Implemented & Verified

### 2.1 API Design

- ✅ RESTful resource-based URL structure (`/api/v1/{resource}`)
- ✅ All endpoints return a consistent `ApiResponse<T>` envelope: `{ success, data, message, errors[] }`
- ✅ HTTP status codes are correct: 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found
- ✅ Paginated responses use `PagedResponse<T>` (page, size, totalElements, totalPages, last)
- ✅ All controllers use `@Valid` for request body validation
- ✅ `GlobalExceptionHandler` converts all exception types to the standard envelope

### 2.2 Authentication & Authorisation

- ✅ JWT issued on login (`POST /api/v1/auth/login`), stored in `localStorage` on the client
- ✅ `JwtAuthenticationFilter` validates the token on every request
- ✅ `ProtectedRoute` component guards all private frontend pages; unauthenticated users are redirected to home with the login modal opened
- ✅ 401 responses trigger client-side `auth:logout` event → token cleared → user returned to home
- ✅ Session timeout modal monitors inactivity and warns before auto-logout
- ✅ Public endpoints explicitly listed in `SecurityConfig.PUBLIC_PATHS`
- ✅ Cross-user access prevented: all service methods verify `userId` ownership before operating on resources

### 2.3 Input Validation

**Backend (Jakarta Validation)**

| Field | Constraint |
|---|---|
| firstName / lastName | `@NotBlank`, `@Size(max=100)` |
| email | `@NotBlank`, `@Email`, `@Size(max=255)` |
| phoneNumber | `@Pattern(regexp = "^\\+91[6-9]\\d{9}$")` |
| pinCode | `@Pattern(regexp = "^\\d{6}$")` |
| password | `@Size(min=8, max=64)`, `@Pattern` (uppercase + digit + special char) |
| cardNumber | `@Pattern(regexp = "^\\d{13,19}$")` — validated in-flight, never stored |
| cvv | `@Pattern(regexp = "^\\d{3,4}$")` — validated in-flight, never stored |
| expiryDate | `@Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{4}$")` — never stored |
| message (contact) | `@NotBlank`, `@Size(max=5000)` |

**Frontend (shared `validators.js`)**

- `isValidEmail`, `isValidPhone`, `isValidPassword`, `isValidPinCode`
- `validateAddressForm` — reused across Checkout, ProfilePage Address Dialog, Add/Edit Address
- `validateProfileForm` — reused in ProfileTab
- All form inputs show inline error messages per field
- Phone/PIN fields enforce digit-only input and `maxLength` at keystroke level
- Card number auto-formatted as `XXXX-XXXX-XXXX-XXXX`, capped at 16 digits
- CVV capped at 3 digits, expiry auto-inserts `/` and clamps month to 01–12

### 2.4 Security

- ✅ **No hardcoded secrets in source code** — all sensitive values (`JWT_SECRET`, `DB_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`) are environment variables
- ✅ **Card data never persisted** — `CardDetailsRequest` fields are validated in-flight and discarded; `Payment` entity stores only `paymentMethod`, `payableAmount`, `status`, `transactionReference` (a mock UUID), and `paidAt`
- ✅ **Passwords BCrypt-hashed** — `BCryptPasswordEncoder` used throughout
- ✅ **No sensitive data in logs** — card numbers, CVV, and raw passwords are not logged anywhere
- ✅ **SQL injection prevention** — all queries use Spring Data JPA / JPQL with parameterised bindings
- ✅ **CSRF disabled** — correct for a stateless JWT API
- ✅ **CORS** — configurable via `CORS_ALLOWED_ORIGIN` environment variable; defaults to `http://localhost:3000`
- ✅ **HTML injection prevention in emails** — `escapeHtml()` applied to all user-supplied contact form fields before embedding in email HTML

### 2.5 Database

- ✅ All entities use proper JPA relationships with named foreign key constraints
- ✅ Transactions scoped correctly — `@Transactional(readOnly = true)` for read paths, `@Transactional` for write paths
- ✅ Lazy loading used correctly; N+1 prevented with `JOIN FETCH` in query methods where needed
- ✅ Dev profile uses `create-drop` (seeds data.sql on every start); Prod profile uses `validate` (schema must exist)
- ✅ Connection pool configured via HikariCP
- ✅ Seed data (`data.sql`) is idempotent for dev

**Entity relationships verified:**

| Entity | Relationships |
|---|---|
| User | Cart (1:1), Wishlist (1:1), Orders (1:N), Addresses (1:N), Reviews (1:N) |
| Order | OrderItems (1:N), Payment (1:1), DeliveryAddressSnapshot (JSON column) |
| Cart | CartItems (1:N → Book) |
| Wishlist | WishlistItems (1:N → Book) |
| Book | Author (N:1), Publisher (N:1), Genres (N:M), Reviews (1:N) |

### 2.6 Frontend Architecture

- ✅ All API calls centralised in `src/common/api/` modules; no inline fetch/axios calls in components
- ✅ `axiosClient` handles JWT attachment and 401 interception globally
- ✅ `VITE_API_BASE_URL` controls the backend URL — no `localhost` hardcoded outside dev fallback
- ✅ `BrowserRouter` used — requires server-side catch-all (`/* → index.html`) for deep-link refreshes
- ✅ React Query used for server state; local form state managed with `useState`
- ✅ Toast notifications (MUI Snackbar stack) reused consistently across all pages
- ✅ `EmptyState` / `LoadingSpinner` / `ErrorMessage` components used consistently
- ✅ Theme defined once in `src/theme/theme.js` — dark mode, palette, typography

### 2.7 Email

- ✅ `JavaMailSender` + `JavaMailSenderAutoConfiguration` (Spring Boot autoconfigures from `spring.mail.*`)
- ✅ Welcome email on registration (async, fire-and-forget)
- ✅ Contact Us form email forwarded to `teambookstore26@gmail.com` (synchronous — errors propagate to the API response)
- ✅ All email HTML uses inline styles (Mailtrap-compatible)
- ✅ Credentials supplied via `MAIL_USERNAME` / `MAIL_PASSWORD` environment variables — not hardcoded

### 2.8 Error Handling

- ✅ Backend: `GlobalExceptionHandler` catches all exception types; no raw 500s leak to clients
- ✅ Frontend: all mutations and queries have `onError` handlers; errors surface via toast notifications
- ✅ Form submissions keep user data on failure (no silent reset)
- ✅ Duplicate submission prevention: submit buttons disabled while request is in-flight

---

## 3. Partially Implemented

### 3.1 Password Reset

- **Status:** Phase 1 placeholder
- `POST /api/v1/auth/forgot-password` returns a reset token in the response body (should be emailed)
- `POST /api/v1/auth/reset-password` validates fields but does not persist the new password (no token-to-user store)
- **Impact:** Forgot Password UI exists but does not complete the full reset flow
- **Required for production:** Implement `PasswordResetToken` entity or Redis cache and send token via email

### 3.2 Card Validation — Expiry Past-Month Check

- Frontend clamps expiry year ≥ current year; does not prevent current-year + past-month at input time
- Submit-time validation correctly rejects past months
- **Impact:** Minor UX only — validation fires correctly on submission

---

## 4. Not Applicable / Out of Scope

- **Payment gateway integration** — intentionally mock (see §6)
- **Admin panel** — not in scope for this portfolio
- **Rate limiting** — not implemented; acceptable for a portfolio/demo
- **CDN / image optimisation** — book cover images are external URLs
- **i18n / localisation** — English only
- **Automated E2E tests** — manual testing only; one Spring Boot context test exists

---

## 5. Future Recommendations

These are improvements for a production-grade system, not blockers for this portfolio deployment:

1. **Password reset** — implement email-based token flow (Phase 2)
2. **Real payment gateway** — integrate Razorpay or Stripe (Phase 2)
3. **Code splitting** — bundle is ~738 kB; dynamic `import()` per route would improve initial load
4. **Refresh tokens** — current JWT is single-use 24h; add refresh token rotation for better UX
5. **Rate limiting** — add `Bucket4j` or Spring Cloud Gateway throttling on auth endpoints
6. **HTTPS enforcement** — configure `server.ssl` or use a reverse proxy (Nginx/Render) in production
7. **Structured logging** — add correlation IDs for request tracing in production
8. **Accessibility** — add ARIA labels and keyboard navigation review
9. **Content Security Policy** — add CSP headers for the frontend

---

## 6. Demo / Mock Payment Limitation

> **IMPORTANT — Read before deploying to a public audience.**

The BookStore payment flow is a **mock/demo implementation**. No real financial transaction is ever made.

**What happens:**
1. The user enters card / UPI / wallet details on the frontend
2. Card data is validated for format only on both frontend and backend
3. **Card data is never stored in the database** — `Payment` entity holds only method, amount, status, and a mock UUID reference
4. Backend `PaymentService` simulates success for all well-formed requests
5. Special test values trigger simulated failures: CVV `000`, card ending `0000`, UPI starting with `fail@` or `decline@`

**Demo failure test cases:**
| Input | Outcome |
|---|---|
| Any valid card details | Payment succeeds |
| CVV = `000` | Payment declined |
| Card number ending in `0000` | Payment declined |
| UPI = `fail@upi` | UPI payment rejected |
| UPI = `decline@upi` | UPI payment rejected |

**Before going live with real payments:** Replace `PaymentService.processPayment()` with a real payment gateway SDK (Razorpay, Stripe, PayU) and ensure PCI-DSS compliance.

---

## 7. Deployment Checklist

### Backend Environment Variables (required)

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | HMAC-SHA256 signing secret (min 32 chars) |
| `CORS_ALLOWED_ORIGIN` | Frontend deployment URL (e.g. `https://bookstore.onrender.com`) |
| `FRONTEND_URL` | Same as above (used in email links) |
| `MAIL_USERNAME` | Mailtrap / SMTP username |
| `MAIL_PASSWORD` | Mailtrap / SMTP password |

### Backend Environment Variables (optional)

| Variable | Default | Description |
|---|---|---|
| `JWT_EXPIRATION_MS` | `86400000` | Token lifetime in ms (24 h) |
| `MAIL_HOST` | `sandbox.smtp.mailtrap.io` | SMTP host |
| `MAIL_PORT` | `2525` | SMTP port |
| `DELIVERY_OFFSET_DAYS` | `7` | Estimated delivery offset |
| `TAX_RATE` | `0.12` | Tax rate (12% GST) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Set to `prod` for production |

### Frontend Environment Variables

| Variable | Description |
|---|---|
| `VITE_API_BASE_URL` | Full backend API base URL including `/api/v1` |
| `VITE_APP_NAME` | Application name shown in Navbar (optional) |

### Server-side Routing

The frontend uses `BrowserRouter` (HTML5 history API). The web server / hosting platform **must** serve `index.html` for all routes that don't match a static file. On Render/Netlify this requires a rewrite rule:

```
/* → /index.html  (200)
```

### Profile Activation

Set `SPRING_PROFILES_ACTIVE=prod` to activate `application-prod.yml`, which:
- Uses `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` (no defaults)
- Uses `ddl-auto: validate` (schema must already exist)
- Disables Swagger UI and API docs
- Sets log levels to WARN/INFO (no SQL debug output)
