# Software Requirements Specification (SRS)
## Book Store — E-Commerce Book Store Application

**Version:** 1.0  
**Date:** July 2025  
**Project Type:** AI-Assisted Full-Stack eCommerce Application  
**Repository:** book-store  

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Business Objective](#2-business-objective)
3. [Functional Requirements](#3-functional-requirements)
4. [Non-Functional Requirements](#4-non-functional-requirements)
5. [User Roles](#5-user-roles)
6. [User Stories](#6-user-stories)
7. [Major Modules](#7-major-modules)
8. [Business Workflow](#8-business-workflow)
9. [Assumptions](#9-assumptions)
10. [Out-of-Scope Features](#10-out-of-scope-features)
11. [Suggested Technology Stack](#11-suggested-technology-stack)

---

## 1. Project Overview

**Book Store** is a full-stack, web-based eCommerce platform built to enable users to discover, browse, and purchase books online. The application is branded as a GenAI Proof of Concept (PoC), demonstrating how AI-assisted development practices can accelerate the delivery of a complete, production-quality digital storefront.

The platform supports the end-to-end customer journey — from unauthenticated browsing and discovery through authenticated checkout and order confirmation. It targets book readers across India, offering curated content sections, genre-based navigation, multi-format book listings, and a streamlined multi-step purchase flow with multiple payment options.

The application is developed as a single-repository full-stack project with a Java Spring Boot backend, a React/Vite frontend, and a PostgreSQL database.

---

## 2. Business Objective

- Provide an intuitive, responsive online storefront where customers can discover and purchase books across a wide range of genres and formats.
- Drive sales through curated merchandising sections (Recommended for You, Bestsellers this Month, New Launches) powered by purchase history and popularity signals.
- Reduce purchase friction by supporting guest checkout, saved addresses, coupon redemption, and multiple payment methods.
- Build long-term customer loyalty through wishlists, a followed-authors ("My Writers") feature, purchase history with "Buy Again" functionality, and a community review system.
- Serve as a reference implementation for AI-assisted software engineering, demonstrating end-to-end delivery of a commercial-grade application using IBM Bob.

---

## 3. Functional Requirements

### 3.1 Authentication & User Management

| ID | Requirement |
|----|-------------|
| FR-AUTH-01 | The system shall provide a login modal accessible from any page, accepting phone number or e-mail address and password. |
| FR-AUTH-02 | The system shall support new user self-registration via a "Sign Up" link presented within the login modal. |
| FR-AUTH-03 | The system shall provide a "Forgot Password" flow to allow users to reset their password. |
| FR-AUTH-04 | The system shall allow unauthenticated users to browse the catalogue and product detail pages as guests via a "Continue as Guest" option. |
| FR-AUTH-05 | The system shall enforce authentication before a user can add items to a wishlist, place an order, or access order history. |
| FR-AUTH-06 | Authenticated users shall have access to a profile/account icon in the top navigation to manage their account. |

### 3.2 Home Page & Discovery

| ID | Requirement |
|----|-------------|
| FR-HOME-01 | The home page shall display a curated **"Recommended for You"** section showing personalised book suggestions based on the authenticated user's purchase and browsing history. |
| FR-HOME-02 | The home page shall display a **"Bestsellers this Month"** section listing books ranked by sales volume within the current calendar month. |
| FR-HOME-03 | The home page shall display a **"New Launches"** section featuring recently added books. |
| FR-HOME-04 | Each book card in all home page sections shall display: cover image, title, author name (linked), short description, format (Paperback/Hard Cover/eBook), genre tags, price (in INR), and estimated delivery date. |
| FR-HOME-05 | Guest users shall see the "Recommended for You" section populated with globally popular or editorially curated books in the absence of personal history. |

### 3.3 Book Catalogue & Navigation

| ID | Requirement |
|----|-------------|
| FR-CAT-01 | The application shall provide a persistent left sidebar listing all supported genre categories: Romance, Mystery, Science Fiction, Fantasy, Historical, Biography, Self-help, Memoir, Travel, Cooking, Children's, Young Adult, Comics & Graphic Novels, Poetry, Drama, Science, Philosophy, Religion, Language Learning. |
| FR-CAT-02 | Selecting a genre category shall filter the book listing to display only books belonging to that genre. An "All" option shall display the full catalogue. |
| FR-CAT-03 | The catalogue shall support the following filter controls: **Language** (dropdown), **Format** (Paperback, eBook, Hard Cover, etc.), **Price Range**, and **Sort By** (Relevance, Price, Newest, etc.). |
| FR-CAT-04 | The catalogue shall provide a full-text **search bar** allowing users to search by title, author, or keyword. |
| FR-CAT-05 | Each book entry in the catalogue shall display genre tags as clickable links that navigate to the corresponding genre filtered view. |
| FR-CAT-06 | Author names on book cards shall be displayed as links navigating to the author's profile page. |

### 3.4 Book Detail Page

| ID | Requirement |
|----|-------------|
| FR-DETAIL-01 | Selecting a book shall navigate to a dedicated Book Detail Page displaying: cover image (large), title, author (linked), publisher (linked), full description, format, genre tags, price (INR), and estimated delivery date. |
| FR-DETAIL-02 | The Book Detail Page shall display the book's language and star rating alongside total copies sold. |
| FR-DETAIL-03 | The page shall provide an **"Add to Cart"** button and an **"Add to Wishlist"** button. |
| FR-DETAIL-04 | The page shall include an **"About the Writer"** section featuring the author's photo, name, and biographical summary. |
| FR-DETAIL-05 | The page shall include a **Reviews** section displaying existing user reviews (text + star rating) and a form for authenticated users to submit a new review (text field with character counter, star rating selector, Submit button). |
| FR-DETAIL-06 | A **"Related Reads"** sidebar panel shall display books related to the current book's genres, showing cover, title, author, format, genre tags, price, and delivery date for each. |
| FR-DETAIL-07 | The page shall include a breadcrumb navigation trail reflecting the current category path (e.g., Home / Non-Fiction / Self Help). |

### 3.5 Shopping Cart & Checkout

| ID | Requirement |
|----|-------------|
| FR-CART-01 | Users shall be able to add one or more books to a shopping cart. The cart icon in the top navigation shall display a badge with the current item count. |
| FR-CART-02 | The Checkout page shall display all cart items with cover image, title, author, format, genre tags, price, and estimated delivery date. |
| FR-CART-03 | The Checkout page shall provide quantity adjustment controls (increment/decrement) for each cart item. |
| FR-CART-04 | The system shall display a **Grand Total** panel itemising: subtotal (price for N items), tax, delivery charges (shown as "Free" when applicable), applied coupon discount, and the final total amount payable. |
| FR-CART-05 | Users shall be able to enter and apply a **coupon code** in the Grand Total panel to receive a discount on the order total. |
| FR-CART-06 | The Checkout page shall include an **Address** section with fields: First Name, Last Name, Address Line 1, Address Line 2, City, PIN code, Email, Phone Number, State, Country. |
| FR-CART-07 | Authenticated users shall have a **"Use Saved Address"** checkbox that auto-fills the address form with their default saved delivery address. |
| FR-CART-08 | The system shall recommend additional books during checkout based on the user's order history ("Recommends items based on Order History"). |

### 3.6 Payment

| ID | Requirement |
|----|-------------|
| FR-PAY-01 | On clicking "Pay Now" from the checkout page, the system shall present a **Complete Payment** modal displaying the payable amount. |
| FR-PAY-02 | The payment modal shall support the following payment methods selectable via tabs: **Credit Card**, **Debit Card**, **UPI**, **Wallet**. |
| FR-PAY-03 | For Credit Card and Debit Card methods, the modal shall collect: Card Number, Name on Card, CVV, and Date of Expiry. |
| FR-PAY-04 | For UPI, the modal shall collect the user's UPI ID. |
| FR-PAY-05 | For Wallet, the system shall display available wallet balance and allow the user to initiate payment. |
| FR-PAY-06 | Upon successful payment, the system shall display an **Order Confirmation** screen with a success indicator (green checkmark), a summary of purchased books (cover, title, author, format, genre tags, price, estimated delivery date), and a "Continue your Shopping" CTA. |
| FR-PAY-07 | The system shall generate and associate a unique Order ID with each confirmed purchase. |

### 3.7 Order Management

| ID | Requirement |
|----|-------------|
| FR-ORDER-01 | Authenticated users shall be able to access **"My Orders"** from the top navigation to view their full purchase history. |
| FR-ORDER-02 | Each order in the history shall display order date, items purchased, total amount, order status, and estimated/actual delivery date. |
| FR-ORDER-03 | The system shall provide a **"Buy Again"** feature on past orders, allowing users to re-add previously purchased items to the cart in a single action. |

### 3.8 Wishlist

| ID | Requirement |
|----|-------------|
| FR-WISH-01 | Authenticated users shall be able to add books to a personal **Wishlist** from the Book Detail Page. |
| FR-WISH-02 | The **"My Wishlist"** page (accessible from top navigation) shall display all wishlisted books with options to move to cart or remove from wishlist. |
| FR-WISH-03 | The wishlist shall persist across sessions for authenticated users. |

### 3.9 My Writers (Followed Authors)

| ID | Requirement |
|----|-------------|
| FR-WRITER-01 | Authenticated users shall be able to follow authors, creating a personalised **"My Writers"** list accessible from the top navigation. |
| FR-WRITER-02 | The My Writers page shall display followed authors with their bio summary and a listing of their books available in the store. |
| FR-WRITER-03 | New book releases or updates from followed authors shall be surfaced in the user's Recommended for You section. |

### 3.10 Author Profile

| ID | Requirement |
|----|-------------|
| FR-AUTHOR-01 | Clicking an author's name (from any book card or detail page) shall navigate to a dedicated Author Profile page. |
| FR-AUTHOR-02 | The Author Profile page shall display: author photo, name, biography, and a list of all books by that author available in the store. |

---

## 4. Non-Functional Requirements

### 4.1 Performance

| ID | Requirement |
|----|-------------|
| NFR-PERF-01 | All page loads and API responses shall complete within **2 seconds** under normal load (up to 500 concurrent users). |
| NFR-PERF-02 | Search and filter operations shall return results within **1 second** for catalogue sizes up to 100,000 books. |
| NFR-PERF-03 | The checkout and payment flows shall be optimised to complete order placement within **3 seconds** of payment confirmation. |

### 4.2 Security

| ID | Requirement |
|----|-------------|
| NFR-SEC-01 | All API endpoints (except public catalogue and home page) shall be protected with JWT-based authentication via Spring Security. |
| NFR-SEC-02 | User passwords shall be stored as bcrypt-hashed values; plaintext passwords shall never be persisted or logged. |
| NFR-SEC-03 | All client-server communication shall be over HTTPS (TLS 1.2+). |
| NFR-SEC-04 | Payment card data shall never be persisted on the application server; the payment gateway shall handle PCI-DSS compliance. |
| NFR-SEC-05 | The application shall implement protection against OWASP Top 10 vulnerabilities including SQL injection, XSS, and CSRF. |
| NFR-SEC-06 | API rate limiting shall be enforced to prevent abuse of authentication and checkout endpoints. |

### 4.3 Usability

| ID | Requirement |
|----|-------------|
| NFR-UX-01 | The application shall be fully functional on modern desktop browsers (Chrome, Firefox, Safari, Edge — latest two versions). |
| NFR-UX-02 | The UI shall be responsive and usable on tablet viewports (768px and above). |
| NFR-UX-03 | All interactive elements shall provide clear visual feedback (hover states, loading spinners, success/error messages). |
| NFR-UX-04 | Form validation errors shall be displayed inline, adjacent to the relevant input field, in real time. |

### 4.4 Reliability & Availability

| ID | Requirement |
|----|-------------|
| NFR-REL-01 | The system shall target **99.5% uptime** during business hours. |
| NFR-REL-02 | Failed payment attempts shall not result in duplicate order records or double charges. |
| NFR-REL-03 | All database transactions shall be ACID-compliant; partial order placements shall be rolled back on failure. |

### 4.5 Maintainability

| ID | Requirement |
|----|-------------|
| NFR-MAINT-01 | The backend shall follow a layered architecture (Controller → Service → Repository) with clearly separated concerns. |
| NFR-MAINT-02 | The frontend shall be component-based, with reusable UI components for book cards, filters, modals, and forms. |
| NFR-MAINT-03 | All REST API contracts shall be documented using OpenAPI 3 / Swagger. |
| NFR-MAINT-04 | The codebase shall maintain a minimum unit test coverage of **70%** for service-layer business logic. |

### 4.6 Scalability

| ID | Requirement |
|----|-------------|
| NFR-SCALE-01 | The backend shall be stateless to support horizontal scaling behind a load balancer. |
| NFR-SCALE-02 | Database connection pooling shall be configured to handle burst traffic without exhausting connections. |

---

## 5. User Roles

| Role | Description | Access Level |
|------|-------------|--------------|
| **Guest User** | An unauthenticated visitor who browses the catalogue without logging in. | Read-only access to home, catalogue, and book detail pages. Cannot add to wishlist, place orders, or view order history. |
| **Registered Customer** | An authenticated user with a full account. | Full access to all customer-facing features: browsing, cart, wishlist, checkout, payment, order history, reviews, and My Writers. |
| **Admin** *(inferred)* | A back-office operator responsible for managing the catalogue, authors, orders, and users. | Access to an admin console for product, order, and user management. (Admin UI is out of scope for this phase.) |

---

## 6. User Stories

### Authentication
- **US-001:** As a new visitor, I want to register for an account using my email/phone and password, so that I can access personalised features.
- **US-002:** As a registered user, I want to log in with my email/phone and password, so that I can access my account, orders, and wishlist.
- **US-003:** As a registered user, I want to reset my forgotten password, so that I can regain access to my account.
- **US-004:** As a visitor, I want to browse books without registering, so that I can evaluate the store before committing to sign up.

### Discovery & Browsing
- **US-005:** As a customer, I want to see personalised book recommendations on the home page, so that I can quickly find books that match my interests.
- **US-006:** As a customer, I want to browse bestsellers and new launches, so that I can discover popular and recently added books.
- **US-007:** As a customer, I want to filter books by genre from a sidebar, so that I can narrow down the catalogue to my preferred category.
- **US-008:** As a customer, I want to filter books by language, format, and price range, so that I can find books that fit my preferences and budget.
- **US-009:** As a customer, I want to search for books by title, author, or keyword, so that I can find a specific book quickly.
- **US-010:** As a customer, I want to click on a genre tag on a book card, so that I can see all books in that genre.

### Book Detail
- **US-011:** As a customer, I want to view the full details of a book including description, author bio, format, rating, and reviews, so that I can make an informed purchase decision.
- **US-012:** As a customer, I want to see related book recommendations on the detail page, so that I can discover other books I might enjoy.
- **US-013:** As a registered customer, I want to submit a star rating and written review for a book I have purchased, so that I can share my opinion with other readers.

### Cart & Checkout
- **US-014:** As a customer, I want to add books to my shopping cart, so that I can purchase multiple books in a single transaction.
- **US-015:** As a customer, I want to adjust the quantity of items in my cart, so that I can buy multiple copies of the same book.
- **US-016:** As a registered customer, I want to save my delivery address to my profile, so that I don't have to re-enter it at every checkout.
- **US-017:** As a customer, I want to apply a coupon code at checkout, so that I can redeem a discount on my order.
- **US-018:** As a customer, I want to see a clear price breakdown (subtotal, tax, delivery, discount, total) before paying, so that I know exactly what I am being charged.

### Payment
- **US-019:** As a customer, I want to pay using my credit card, debit card, UPI, or digital wallet, so that I can choose the most convenient payment method.
- **US-020:** As a customer, I want to receive a purchase confirmation with a summary of my order, so that I have proof of my purchase and know when to expect delivery.

### Order Management
- **US-021:** As a registered customer, I want to view my full order history, so that I can track past purchases and review what I have bought.
- **US-022:** As a registered customer, I want to use the "Buy Again" feature on a past order, so that I can quickly repurchase a book I have previously bought.

### Wishlist & Authors
- **US-023:** As a registered customer, I want to add books to my wishlist, so that I can save books I am interested in for a future purchase.
- **US-024:** As a registered customer, I want to move a book from my wishlist to my cart, so that I can purchase it when I am ready.
- **US-025:** As a registered customer, I want to follow authors I like, so that I can stay updated on their new releases.
- **US-026:** As a registered customer, I want to view a list of all books by a followed author, so that I can explore their full catalogue.

---

## 7. Major Modules

### Module 1: Authentication & User Profile
Handles user registration, login (email/phone + password), JWT token issuance, password reset, and management of user profile data including saved delivery addresses.

### Module 2: Book Catalogue
Manages the book inventory including metadata (title, author, publisher, description, cover image, format, language, genre tags, price), category-based navigation, full-text search, and multi-dimensional filtering (language, format, price range, sort order).

### Module 3: Home Page & Personalisation
Renders the curated home page sections: **Recommended for You** (personalised), **Bestsellers this Month** (sales-ranked), and **New Launches** (recency-ranked). Applies fallback editorial curation for guest users.

### Module 4: Book Detail & Author Profiles
Delivers the full book detail view including description, author bio section, language/rating/sales data, and the Related Reads sidebar. Hosts author profile pages showing biography and full book listing.

### Module 5: Reviews & Ratings
Allows authenticated customers to submit star ratings and text reviews for books. Displays aggregated star rating and individual reviews on the book detail page.

### Module 6: Shopping Cart
Manages the in-session and persistent cart for authenticated users, including item addition, quantity adjustment, and cart badge count in the navigation.

### Module 7: Checkout & Order Placement
Orchestrates the checkout flow: cart review, delivery address entry/selection, coupon application, price breakdown computation (subtotal, tax, delivery, discount, total), and order record creation.

### Module 8: Payment Processing
Integrates with a payment gateway to process Credit Card, Debit Card, UPI, and Wallet transactions. Displays the payable amount, collects payment credentials in a secure modal, and handles success/failure outcomes.

### Module 9: Order Management
Provides the customer-facing **My Orders** page with full purchase history, order status tracking, and the "Buy Again" shortcut to re-add past order items to the cart.

### Module 10: Wishlist
Manages each authenticated user's personal wishlist — adding/removing books and moving items to cart.

### Module 11: My Writers (Author Following)
Allows users to follow and unfollow authors. Surfaces followed-author content in the recommendation engine and provides a dedicated **My Writers** page.

### Module 12: Admin Catalogue Management *(Backend only — Phase 2)*
Back-office APIs for creating, updating, and deactivating book listings, managing author profiles, and moderating reviews. No admin frontend UI in this phase.

---

## 8. Business Workflow

### 8.1 Guest Browsing Flow
```
Landing on Home Page
  → View Recommended / Bestsellers / New Launches
  → [Optional] Select Genre from Sidebar
  → [Optional] Apply Filters (Language, Format, Price Range)
  → [Optional] Search by keyword
  → View Book Detail Page
  → Prompt to Login / Register when attempting to Add to Cart or Wishlist
```

### 8.2 New User Registration & Login Flow
```
Click Login Icon  →  Login Modal appears
  → "Sign Up Here"  →  Registration Form  →  Account Created  →  Auto-login
  → OR: Enter Credentials  →  JWT Issued  →  Redirected to previous page
  → OR: "Continue as Guest"  →  Browse without account
```

### 8.3 Book Purchase Flow
```
Browse Catalogue / Home Page
  → Select Book  →  Book Detail Page
    → "Add to Cart"  →  Cart badge updates
  → [Repeat for multiple books]
  → Navigate to Cart / Checkout Page
    → Review items and quantities (adjust with +/−)
    → Enter / Select Saved Delivery Address
    → [Optional] Apply Coupon Code  →  Discount applied to Grand Total
    → Review Grand Total (subtotal + tax + delivery − discount)
    → Click "Pay Now"
      → Payment Modal
        → Select Method (Credit Card / Debit Card / UPI / Wallet)
        → Enter payment credentials
        → Click "Pay Now"
          → Payment Gateway processes transaction
            → SUCCESS → Order Confirmation Screen (green checkmark + order summary)
                       → "Continue your Shopping" returns to Home
            → FAILURE → Error message displayed in payment modal, user may retry
```

### 8.4 Order History & Buy Again Flow
```
Top Navigation  →  "My Orders"
  → View order list (date, items, status, total)
  → Select past order  →  View order detail
  → Click "Buy Again"  →  Items added to current cart
  → Proceed through standard purchase flow
```

### 8.5 Wishlist Flow
```
Book Detail Page  →  "Add to Wishlist"
  → Book saved to My Wishlist (persisted for authenticated users)
Top Navigation  →  "My Wishlist"
  → View wishlisted books
  → "Move to Cart"  →  Item transferred to cart
  → "Remove"  →  Item removed from wishlist
```

### 8.6 Author Follow Flow
```
Click Author Name (any book card or detail page)  →  Author Profile Page
  → "Follow Author"  →  Author added to My Writers list
Top Navigation  →  "My Writers"
  → View followed authors and their books
  → Followed-author new releases appear in "Recommended for You"
```

---

## 9. Assumptions

1. **Currency:** All prices are displayed in Indian Rupees (INR, ₹). The platform is targeted at the Indian market.
2. **Physical Delivery Only (Phase 1):** The initial scope covers physical book formats (Paperback, Hard Cover). eBook delivery/download is listed as a format option but digital fulfilment is deferred to a later phase.
3. **Single Storefront:** The application operates as a single storefront (not a marketplace); all books are sold by a single retailer.
4. **Payment Gateway Integration:** The payment processing UI is simulated/mocked in Phase 1. Integration with a production payment gateway (e.g., Razorpay, PayU) is assumed for Phase 2.
5. **Delivery Date Calculation:** Estimated delivery dates displayed on book cards and detail pages are calculated by the backend based on a predefined rule (e.g., current date + N business days) and not integrated with a live logistics partner in Phase 1.
6. **Tax Calculation:** Tax amounts are computed server-side using a fixed rate applied to the order subtotal.
7. **Coupon Management:** Coupons are pre-configured in the system by the admin. Dynamic coupon generation is out of scope for Phase 1.
8. **Review Eligibility:** Any authenticated user may submit a review in Phase 1. Purchase-verified review gating is deferred to Phase 2.
9. **Search Engine:** Full-text search is implemented using PostgreSQL's native full-text search capabilities in Phase 1. Integration with a dedicated search engine (Elasticsearch) is deferred.
10. **Recommendation Engine:** "Recommended for You" personalisation in Phase 1 is based on simple rules (same genre as past purchases/browsing). A machine-learning recommendation engine is out of scope.
11. **Single Language UI:** The application interface is in English only in Phase 1.
12. **Browser Support:** The application targets desktop browsers only in Phase 1; mobile-responsive layout is aspirational but not a hard requirement for initial delivery.

---

## 10. Out-of-Scope Features

The following features are explicitly excluded from the Phase 1 delivery of this SRS:

| # | Feature | Rationale |
|---|---------|-----------|
| 1 | **Admin Frontend UI** | Back-office catalogue, user, and order management UI is deferred to Phase 2. Backend admin APIs may be included. |
| 2 | **eBook / Digital Download Fulfilment** | Digital delivery of purchased eBooks (file download, DRM) is out of scope. |
| 3 | **Mobile Native Applications** | iOS and Android native apps are not in scope. |
| 4 | **Live Logistics / Shipment Tracking** | Real-time order tracking integrated with courier partners is out of scope. |
| 5 | **Production Payment Gateway Integration** | Live payment processing via Razorpay/PayU/Stripe is deferred; payment flow is simulated in Phase 1. |
| 6 | **Email / SMS Notifications** | Transactional notifications (order confirmation, shipping update, password reset email) are out of scope for Phase 1. |
| 7 | **Multi-Vendor / Marketplace** | The platform operates as a single-seller store; marketplace functionality is not in scope. |
| 8 | **Loyalty / Gift Points Programme** | A gift points or loyalty rewards programme (beyond coupon code redemption) is out of scope. |
| 9 | **Social Sharing** | Sharing books or reviews to social media platforms is out of scope. |
| 10 | **Book Subscription / Membership Plans** | Recurring subscription models for book access are out of scope. |
| 11 | **ML-Based Recommendation Engine** | Advanced machine-learning personalisation is out of scope; rule-based recommendations are used in Phase 1. |
| 12 | **Multi-Language / Internationalisation (i18n)** | The UI will be English-only; multi-language support is deferred. |
| 13 | **Returns & Refunds Management** | Order cancellation and refund processing workflows are out of scope for Phase 1. |
| 14 | **Inventory / Stock Management** | Real-time stock level tracking and out-of-stock handling are deferred to Phase 2. |

---

## 11. Suggested Technology Stack

| Layer | Technology | Justification |
|-------|-----------|---------------|
| **Frontend Framework** | React 18 (with Vite) | Component-based SPA, fast HMR with Vite, large ecosystem for eCommerce patterns. |
| **Frontend UI Library** | Material UI (MUI) v5 | Consistent, accessible design system; dark-themed components align with the Book Store UI aesthetics visible in screenshots. |
| **Frontend State Management** | React Context API + React Query | Context for auth/cart global state; React Query for server-state caching and synchronisation. |
| **Frontend Routing** | React Router v6 | Declarative client-side routing with nested routes for catalogue, detail, and checkout pages. |
| **Backend Framework** | Spring Boot 3 (Java 17) | Production-grade REST API framework with auto-configuration, embedded Tomcat, and extensive ecosystem. |
| **Security** | Spring Security 6 + JWT (JJWT) | Stateless JWT-based authentication; role-based access control via method-level security annotations. |
| **Data Access** | Spring Data JPA + Hibernate | ORM-based data access layer; JPA repositories reduce boilerplate for CRUD and complex queries. |
| **Database** | PostgreSQL 15 | ACID-compliant relational database; native full-text search for Phase 1 catalogue search. |
| **Database Migration** | Flyway | Version-controlled schema migrations integrated with Spring Boot startup. |
| **API Documentation** | Springdoc OpenAPI 3 (Swagger UI) | Auto-generated, interactive API documentation from Spring annotations. |
| **Build Tool (Backend)** | Apache Maven | Standard dependency management and build lifecycle for Spring Boot projects. |
| **Build Tool (Frontend)** | Vite | Fast bundler and dev server optimised for React. |
| **API Communication** | REST (JSON over HTTPS) | Stateless, cacheable, widely supported; straightforward integration between React frontend and Spring Boot backend. |
| **Version Control** | Git / GitHub | Single monorepo (`book-store`) hosting both frontend and backend source trees. |
| **Containerisation** *(recommended)* | Docker + Docker Compose | Consistent local development environment; compose file to orchestrate app + PostgreSQL containers. |
| **Testing (Backend)** | JUnit 5 + Mockito + Spring Boot Test | Unit tests for service layer; integration tests for controllers with an in-memory or Testcontainers PostgreSQL instance. |
| **Testing (Frontend)** | Vitest + React Testing Library | Fast unit/component tests aligned with the Vite toolchain. |

---

*This document was generated as part of the AI-Assisted Software Development project using IBM Bob.*  
*Document Status: Draft v1.0 — Pending stakeholder review and sign-off.*
