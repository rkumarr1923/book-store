package com.bookstore.repository;

import com.bookstore.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Persistence operations for {@link OrderItem}.
 *
 * Query methods cover:
 * - Fetching all items for a given order (used for "Buy Again" feature)
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // ── Order items listing ───────────────────────────────────────────────────

    /**
     * Return all items in a given order.
     * Used by the "Buy Again" feature to re-add past order items to the cart.
     */
    List<OrderItem> findByOrderId(Long orderId);
}
