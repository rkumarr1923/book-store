package com.bookstore.repository;

import com.bookstore.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Genre}.
 *
 * Query methods cover:
 * - Listing all genres (for the genre sidebar)
 * - Lookup by URL-safe slug (for genre navigation)
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    /** Find a genre by its URL-safe slug. Used by the genre navigation / filtering endpoint. */
    Optional<Genre> findBySlug(String slug);

    /** Return all genres ordered alphabetically. Used by the genre list endpoint. */
    List<Genre> findAllByOrderByNameAsc();
}
