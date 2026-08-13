package com.bookstore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds all app.* properties from application.yml into a strongly-typed config bean.
 * Provides JWT settings and other application-level configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Delivery delivery = new Delivery();
    private final Tax tax = new Tax();
    private final PasswordReset passwordReset = new PasswordReset();

    // ── Getters ───────────────────────────────────────────────────────────────

    public Jwt getJwt() { return jwt; }
    public Delivery getDelivery() { return delivery; }
    public Tax getTax() { return tax; }
    public PasswordReset getPasswordReset() { return passwordReset; }

    // ── Nested config classes ─────────────────────────────────────────────────

    public static class Jwt {
        /** HMAC-SHA signing secret. Must be at least 256 bits for HS256. */
        private String secret;
        /** Token lifetime in milliseconds (default: 86400000 = 24 h). */
        private long expirationMs = 86_400_000L;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    }

    public static class Delivery {
        /** Business days added to today for the estimated delivery date. */
        private int offsetDays = 7;

        public int getOffsetDays() { return offsetDays; }
        public void setOffsetDays(int offsetDays) { this.offsetDays = offsetDays; }
    }

    public static class Tax {
        /** Fractional tax rate applied to the order subtotal (e.g. 0.12 = 12%). */
        private double rate = 0.12;

        public double getRate() { return rate; }
        public void setRate(double rate) { this.rate = rate; }
    }

    public static class PasswordReset {
        /** Seconds until a password-reset token expires (default: 900 = 15 min). */
        private int tokenExpirySeconds = 900;

        public int getTokenExpirySeconds() { return tokenExpirySeconds; }
        public void setTokenExpirySeconds(int tokenExpirySeconds) {
            this.tokenExpirySeconds = tokenExpirySeconds;
        }
    }
}
