package com.bookstore.repository;

import com.bookstore.domain.Order;
import com.bookstore.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Order}.
 *
 * Query methods cover:
 * - Paginated order history for a user
 * - Single order lookup with ownership verification
 * - Order lookup by order number
 * - Eager-fetching order with items and payment in a single query to avoid N+1
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ── Order history ─────────────────────────────────────────────────────────

    /** Return paginated orders for a user, ordered by placement date descending. */
    Page<Order> findByUserIdOrderByPlacedAtDesc(Long userId, Pageable pageable);

    /** Return paginated confirmed orders for a user (excluding unconfirmed PLACED drafts), ordered by placement date descending. */
    Page<Order> findByUserIdAndStatusNotOrderByPlacedAtDesc(Long userId, OrderStatus status, Pageable pageable);

    // ── Single order lookup ───────────────────────────────────────────────────

    /** Find a specific order that belongs to the given user. Prevents cross-user access. */
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    /** Find an order by its human-readable order number. */
    Optional<Order> findByOrderNumber(String orderNumber);

    /** Check whether an order number is already in use. */
    boolean existsByOrderNumber(String orderNumber);

    // ── Eager-loaded order detail ─────────────────────────────────────────────

    /**
     * Fetch an order with its items, books per item, and payment record
     * in a single query. Avoids N+1 when rendering the order detail page.
     */
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items oi
            LEFT JOIN FETCH oi.book
            LEFT JOIN FETCH o.payment
            WHERE o.id = :orderId AND o.user.id = :userId
            """)
    Optional<Order> findByIdAndUserIdWithDetails(@Param("orderId") Long orderId,
                                                 @Param("userId") Long userId);

    // ── Status filter ─────────────────────────────────────────────────────────

    /** Return paginated orders for a user filtered by status. */
    Page<Order> findByUserIdAndStatusOrderByPlacedAtDesc(Long userId, OrderStatus status,
                                                          Pageable pageable);

    // ── Coupon usage tracking ─────────────────────────────────────────────────

    /**
     * Count how many times a user has successfully used a coupon.
     * Only CONFIRMED, SHIPPED, and DELIVERED orders are counted —
     * PLACED (awaiting payment) and CANCELLED orders are excluded.
     * This is the source-of-truth for per-user coupon usage limits.
     */
    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.user.id  = :userId
              AND o.coupon.id = :couponId
              AND o.status IN :successStatuses
            """)
    long countSuccessfulCouponUsages(
            @Param("userId")         Long userId,
            @Param("couponId")       Long couponId,
            @Param("successStatuses") List<OrderStatus> successStatuses);
}
