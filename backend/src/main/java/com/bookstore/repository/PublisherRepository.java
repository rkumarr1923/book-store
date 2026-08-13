package com.bookstore.repository;

import com.bookstore.domain.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Persistence operations for {@link Publisher}.
 *
 * Query methods cover:
 * - Publisher lookup by name
 * - Duplicate name check on admin creation
 */
@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    /** Find a publisher by exact name. Used for duplicate prevention. */
    Optional<Publisher> findByName(String name);

    /** Returns true if a publisher with the given name already exists. */
    boolean existsByName(String name);
}
