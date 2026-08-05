package com.bookstore.repository;

import com.bookstore.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for {@link Cart}.
 *
 * Query methods cover:
 * - Fetching a user's cart by user ID
 * - Eager-fetching cart with its items and books in a single query to avoid N+1
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // ── Cart lookup ───────────────────────────────────────────────────────────

    /** Find the cart for a given user. */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Fetch the cart with all its items and the associated book for each item
     * in a single JOIN FETCH query. Avoids N+1 when rendering the cart page.
     */
    @Query("""
            SELECT c FROM Cart c
            LEFT JOIN FETCH c.items ci
            LEFT JOIN FETCH ci.book
            WHERE c.user.id = :userId
            """)
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
}
