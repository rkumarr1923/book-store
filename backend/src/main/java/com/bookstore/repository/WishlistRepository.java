package com.bookstore.repository;

import com.bookstore.domain.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for {@link Wishlist}.
 *
 * Query methods cover:
 * - Fetching a user's wishlist by user ID
 * - Eager-fetching wishlist with its items and books in a single query to avoid N+1
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // ── Wishlist lookup ───────────────────────────────────────────────────────

    /** Find the wishlist for a given user. */
    Optional<Wishlist> findByUserId(Long userId);

    /**
     * Fetch the wishlist with all its items and the associated book for each item
     * in a single JOIN FETCH query. Avoids N+1 when rendering the wishlist page.
     */
    @Query("""
            SELECT w FROM Wishlist w
            LEFT JOIN FETCH w.items wi
            LEFT JOIN FETCH wi.book
            WHERE w.user.id = :userId
            """)
    Optional<Wishlist> findByUserIdWithItems(@Param("userId") Long userId);
}
