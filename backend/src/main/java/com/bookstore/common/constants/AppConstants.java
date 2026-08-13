package com.bookstore.common.constants;

/**
 * Central repository for all application-level constants.
 * Group constants into inner static classes by concern.
 */
public final class AppConstants {

    private AppConstants() {
        // Utility class — prevent instantiation
    }

    // ── API ───────────────────────────────────────────────────────────────────

    public static final class Api {
        private Api() {}

        public static final String BASE_PATH    = "/api/v1";
        public static final String AUTH_PATH    = BASE_PATH + "/auth";
        public static final String USERS_PATH   = BASE_PATH + "/users";
        public static final String HOME_PATH    = BASE_PATH + "/home";
        public static final String GENRES_PATH  = BASE_PATH + "/genres";
        public static final String BOOKS_PATH   = BASE_PATH + "/books";
        public static final String AUTHORS_PATH = BASE_PATH + "/authors";
        public static final String CART_PATH    = BASE_PATH + "/cart";
        public static final String CHECKOUT_PATH = BASE_PATH + "/checkout";
        public static final String PAYMENTS_PATH = BASE_PATH + "/payments";
        public static final String ORDERS_PATH  = BASE_PATH + "/orders";
        public static final String WISHLIST_PATH = BASE_PATH + "/wishlist";
        public static final String HEALTH_PATH  = BASE_PATH + "/health";
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    public static final class Pagination {
        private Pagination() {}

        public static final int DEFAULT_PAGE      = 0;
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE     = 50;
        public static final int HOME_SECTION_LIMIT = 10;
        public static final int MAX_HOME_LIMIT    = 20;
        public static final int DEFAULT_RELATED_LIMIT = 6;
        public static final int MAX_RELATED_LIMIT = 12;
    }

    // ── Security ──────────────────────────────────────────────────────────────

    public static final class Security {
        private Security() {}

        public static final String BEARER_PREFIX        = "Bearer ";
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String ROLE_CUSTOMER        = "ROLE_CUSTOMER";
        public static final String ROLE_ADMIN           = "ROLE_ADMIN";
    }

    // ── Validation ────────────────────────────────────────────────────────────

    public static final class Validation {
        private Validation() {}

        public static final int MAX_REVIEW_LENGTH   = 100;
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 64;
        public static final int MAX_COUPON_CODE_LENGTH = 50;
        public static final String PIN_CODE_REGEX   = "^\\d{6}$";
        public static final String PHONE_REGEX      = "^\\+91[6-9]\\d{9}$";
        public static final String CARD_NUMBER_REGEX = "^\\d{13,19}$";
        public static final String CVV_REGEX        = "^\\d{3,4}$";
        public static final String EXPIRY_REGEX     = "^(0[1-9]|1[0-2])/\\d{4}$";
        public static final String UPI_ID_REGEX     = "^[a-zA-Z0-9._-]+@[a-zA-Z]+$";
    }

    // ── Business ──────────────────────────────────────────────────────────────

    public static final class Business {
        private Business() {}

        public static final String ORDER_NUMBER_PREFIX = "BST";
        public static final int    MIN_RATING          = 1;
        public static final int    MAX_RATING          = 5;
        public static final double FREE_DELIVERY_THRESHOLD = 500.0;
    }
}
