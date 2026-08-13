package com.bookstore.repository;

import com.bookstore.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for {@link Author}.
 *
 * Query methods cover:
 * - Author profile lookup by ID (via JpaRepository)
 * - Duplicate name check on admin creation
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    /** Find an author by exact name (case-sensitive). Used for duplicate prevention. */
    Optional<Author> findByName(String name);

    /** Returns true if an author with the given name already exists. */
    boolean existsByName(String name);
}
