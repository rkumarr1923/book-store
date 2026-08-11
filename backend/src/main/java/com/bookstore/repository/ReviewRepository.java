package com.bookstore.repository;

import com.bookstore.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Review}.
 *
 * Query methods cover:
 * - Paginated listing of reviews for a book
 * - Checking whether a user has already reviewed a book (one review per user/book)
 * - Average rating for a book (used for the book detail page)
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ── Book reviews listing ──────────────────────────────────────────────────

    /** Return paginated reviews for a book, ordered by creation time descending. */
    Page<Review> findByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);

    // ── Duplicate review check ────────────────────────────────────────────────

    /** Return the review a specific user left for a specific book, if any. */
    Optional<Review> findByBookIdAndUserId(Long bookId, Long userId);

    /** Returns true if the user has already submitted a review for the given book. */
    boolean existsByBookIdAndUserId(Long bookId, Long userId);

    // ── Aggregate statistics ──────────────────────────────────────────────────

    /** Calculate the average star rating for a book. Returns null if no reviews exist. */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);

    /**
     * Batch-calculate average ratings for a set of book IDs in a single query.
     * Returns one {@code Object[]} row per book that has at least one review,
     * where {@code row[0]} is the book ID ({@code Long}) and {@code row[1]} is
     * the average rating ({@code Double}).
     * Books with no reviews are absent from the result — callers should default
     * those to {@code null}.
     */
    @Query("SELECT r.book.id, AVG(r.rating) FROM Review r WHERE r.book.id IN :bookIds GROUP BY r.book.id")
    List<Object[]> findAverageRatingsByBookIds(@Param("bookIds") Collection<Long> bookIds);

    /** Count the total number of reviews for a book. */
    long countByBookId(Long bookId);
}
