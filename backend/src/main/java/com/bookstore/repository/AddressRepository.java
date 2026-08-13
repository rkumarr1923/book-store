package com.bookstore.repository;

import com.bookstore.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Persistence operations for {@link Address}.
 *
 * Query methods cover:
 * - Listing a user's saved addresses
 * - Ownership verification (address belongs to user)
 * - Default address management
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // ── User address listing ──────────────────────────────────────────────────

    /** Return all addresses belonging to a user, ordered by creation time descending. */
    List<Address> findByUserIdOrderByCreatedAtDesc(Long userId);

    // ── Ownership check ───────────────────────────────────────────────────────

    /** Verify that a specific address belongs to the given user. */
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    // ── Default address management ────────────────────────────────────────────

    /** Find the current default address for a user. */
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Clear the default flag for all addresses belonging to a user.
     * Called before setting a new default to ensure only one is active at a time.
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);
}
