package com.bookstore.repository;

import com.bookstore.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Persistence operations for {@link Coupon}.
 *
 * Query methods cover:
 * - Coupon lookup by code (used during checkout validation)
 * - Active and non-expired coupon check
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // ── Coupon validation ─────────────────────────────────────────────────────

    /** Find a coupon by its code. Used to look up a coupon entered at checkout. */
    Optional<Coupon> findByCode(String code);

    /**
     * Find an active, non-expired coupon by code.
     * Used during checkout validation to confirm the coupon can be applied.
     */
    Optional<Coupon> findByCodeAndIsActiveTrueAndExpiresAtAfter(String code, Instant now);
}
