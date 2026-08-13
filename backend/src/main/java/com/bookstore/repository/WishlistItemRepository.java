package com.bookstore.repository;

import com.bookstore.domain.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for {@link WishlistItem}.
 *
 * Query methods cover:
 * - Paginated listing of wishlist items
 * - Ownership verification (item belongs to a specific wishlist)
 * - Duplicate detection (same book already in wishlist)
 */
@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    // ── Paginated listing ─────────────────────────────────────────────────────

    /** Return a page of wishlist items for the given wishlist, newest first. */
    Page<WishlistItem> findByWishlistIdOrderByCreatedAtDesc(Long wishlistId, Pageable pageable);

    // ── Ownership check ───────────────────────────────────────────────────────

    /** Find a specific wishlist item that belongs to the given wishlist. Used for delete/move. */
    Optional<WishlistItem> findByIdAndWishlistId(Long id, Long wishlistId);

    // ── Duplicate check ───────────────────────────────────────────────────────

    /** Find an existing wishlist item for the same book in the given wishlist. */
    Optional<WishlistItem> findByWishlistIdAndBookId(Long wishlistId, Long bookId);

    /** Returns true if the book is already saved in the given wishlist. */
    boolean existsByWishlistIdAndBookId(Long wishlistId, Long bookId);
}
