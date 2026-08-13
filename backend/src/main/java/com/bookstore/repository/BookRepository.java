package com.bookstore.repository;

import com.bookstore.domain.Book;
import com.bookstore.domain.enums.BookFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Book}.
 *
 * Also extends {@link JpaSpecificationExecutor} to support dynamic catalogue filtering
 * (language, format, genre, price range, author, publisher) via {@code Specification}
 * objects in the service layer.
 *
 * Query methods cover:
 * - Catalogue listing with pagination
 * - Full-text search via PostgreSQL {@code search_vector} (JPQL LIKE fallback for H2 tests)
 * - Home page sections: recommended, bestsellers, new launches
 * - Related books (same author or overlapping genres)
 * - Single book detail lookup
 * - Filtering helpers: by author, genre, format, language, price range
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    // ── Single book lookup ────────────────────────────────────────────────────

    /** Find an active book by its ID. */
    Optional<Book> findByIdAndIsActiveTrue(Long id);

    // ── Home: Bestsellers ─────────────────────────────────────────────────────

    /**
     * Return the top N active books ordered by {@code copiesSold} descending.
     * Used for the Bestsellers section on the home page.
     */
    List<Book> findTop10ByIsActiveTrueOrderByCopiesSoldDesc();

    // ── Home: New Launches ────────────────────────────────────────────────────

    /**
     * Return the top N active books published on or after a given date,
     * ordered by published date descending.
     * Used for the New Launches section on the home page.
     */
    List<Book> findTop10ByIsActiveTrueAndPublishedDateGreaterThanEqualOrderByPublishedDateDesc(
            LocalDate since);

    // ── Home: Recommended ────────────────────────────────────────────────────

    /**
     * Return the top N active books from authors that a user follows,
     * ordered by published date descending.
     * Used as the personalised "Recommended for You" section.
     */
    @Query("""
            SELECT b FROM Book b
            WHERE b.isActive = true
              AND b.author.id IN (
                  SELECT ufa.author.id FROM UserFollowedAuthor ufa
                  WHERE ufa.user.id = :userId
              )
            ORDER BY b.publishedDate DESC
            """)
    List<Book> findRecommendedBooksForUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Fallback recommended books when the user follows no authors —
     * returns the newest active books, ordered by created date descending.
     */
    List<Book> findTop10ByIsActiveTrueOrderByCreatedAtDesc();

    // ── Book search ───────────────────────────────────────────────────────────

    /**
     * JPQL-based title/author name search using LIKE.
     * Used as the primary search method (works with both PostgreSQL and H2 for tests).
     * The service layer can switch to native FTS for production if needed.
     */
    @Query("""
            SELECT DISTINCT b FROM Book b
            JOIN b.author a
            WHERE b.isActive = true
              AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<Book> searchByTitleOrAuthorName(@Param("query") String query, Pageable pageable);

    // ── Catalogue: filtered listing ───────────────────────────────────────────

    /** Return a paginated list of all active books. Used as the base catalogue listing. */
    Page<Book> findByIsActiveTrue(Pageable pageable);

    /** Filter active books by genre. */
    @Query("""
            SELECT DISTINCT b FROM Book b
            JOIN b.genres g
            WHERE b.isActive = true
              AND g.id = :genreId
            """)
    Page<Book> findByIsActiveTrueAndGenreId(@Param("genreId") Long genreId, Pageable pageable);

    /** Filter active books by author. */
    Page<Book> findByIsActiveTrueAndAuthorId(Long authorId, Pageable pageable);

    /** Filter active books by language. */
    Page<Book> findByIsActiveTrueAndLanguageIgnoreCase(String language, Pageable pageable);

    /** Filter active books by format. */
    Page<Book> findByIsActiveTrueAndFormat(BookFormat format, Pageable pageable);

    /** Filter active books by price range (inclusive). */
    Page<Book> findByIsActiveTrueAndPriceBetween(BigDecimal minPrice, BigDecimal maxPrice,
                                                  Pageable pageable);

    // ── Related books ─────────────────────────────────────────────────────────

    /**
     * Return active books by the same author, excluding the current book.
     * Used for the "Related Reads" section on the book detail page.
     */
    @Query("""
            SELECT b FROM Book b
            WHERE b.isActive = true
              AND b.author.id = :authorId
              AND b.id <> :excludeBookId
            ORDER BY b.publishedDate DESC
            """)
    List<Book> findRelatedByAuthor(@Param("authorId") Long authorId,
                                   @Param("excludeBookId") Long excludeBookId,
                                   Pageable pageable);

    /**
     * Return active books that share at least one genre with the given book,
     * excluding the current book itself. Used as a secondary source for related reads.
     */
    @Query("""
            SELECT DISTINCT b FROM Book b
            JOIN b.genres g
            WHERE b.isActive = true
              AND b.id <> :excludeBookId
              AND g.id IN :genreIds
            ORDER BY b.copiesSold DESC
            """)
    List<Book> findRelatedByGenres(@Param("genreIds") List<Long> genreIds,
                                   @Param("excludeBookId") Long excludeBookId,
                                   Pageable pageable);

    // ── Author profile page ───────────────────────────────────────────────────

    /**
     * Return all active books for a given author, ordered by published date descending.
     * Used on the author profile page.
     */
    List<Book> findByIsActiveTrueAndAuthorIdOrderByPublishedDateDesc(Long authorId);
}
