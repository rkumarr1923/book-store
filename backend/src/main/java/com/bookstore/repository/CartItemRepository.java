package com.bookstore.repository;

import com.bookstore.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link CartItem}.
 *
 * Query methods cover:
 * - Ownership verification (item belongs to a specific cart)
 * - Duplicate detection (same book already in cart)
 * - Clearing all items from a cart
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // ── Ownership check ───────────────────────────────────────────────────────

    /** Find a specific cart item that belongs to the given cart. Used for update/delete. */
    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    // ── Duplicate check ───────────────────────────────────────────────────────

    /** Find an existing cart item for the same book in the same cart. */
    Optional<CartItem> findByCartIdAndBookId(Long cartId, Long bookId);

    // ── Cart operations ───────────────────────────────────────────────────────

    /** Return all items in a cart. Used when clearing the cart. */
    List<CartItem> findByCartId(Long cartId);

    /** Delete all items belonging to a cart. Used by the "Clear Cart" endpoint. */
    void deleteByCartId(Long cartId);
}
